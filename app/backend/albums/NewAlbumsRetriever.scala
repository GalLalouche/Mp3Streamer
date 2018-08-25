package backend.albums

import backend.configs.{RealConfig, StandaloneConfig}
import backend.logging.Logger
import backend.mb.MbArtistReconciler
import backend.mb.MbArtistReconciler.MbAlbumMetadata
import backend.recon._
import backend.recon.Reconcilable.SongExtractor
import common.io.IODirectory
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.func.{MoreSeqInstances, MoreTraverseInstances, ToMoreFunctorOps, ToMoreMonadErrorOps, ToTraverseMonadPlusOps}
import common.rich.primitives.RichBoolean._
import models.{IOMusicFinder, Song}
import net.codingwell.scalaguice.InjectorExtensions._
import rx.lang.scala.Observable

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.{Failure, Success}

import scalaz.Kleisli
import scalaz.std.FutureInstances

private class NewAlbumsRetriever(
    reconciler: ReconcilerCacher[Artist],
    albumReconStorage: AlbumReconStorage,
    mf: IOMusicFinder,
)(
    implicit c: RealConfig)
    extends FutureInstances with MoreTraverseInstances with ToMoreFunctorOps
        with ToTraverseMonadPlusOps with ToMoreMonadErrorOps with MoreSeqInstances {
  private val logger = c.injector.instance[Logger]
  private val log = logger.verbose _
  private val meta = new MbArtistReconciler
  private def getExistingAlbums: Seq[Album] = mf.genreDirs
      .flatMap(_.deepDirs)
      .flatMap(NewAlbumsRetriever.dirToAlbum(_))
  private def removeIgnoredAlbums(artist: Artist, albums: Seq[MbAlbumMetadata]): Future[Seq[MbAlbumMetadata]] = {
    def toAlbum(album: MbAlbumMetadata): Album =
      Album(title = album.title, year = album.releaseDate.getYear, artist = artist)
    val isNotIgnored = {
      def isIgnored(album: MbAlbumMetadata): Future[Boolean] =
        toAlbum(album) |> albumReconStorage.isIgnored |> (_.map(_ getOrElse false))
      Kleisli(isIgnored).map(_.isFalse)
    }
    albums filterTraverse isNotIgnored
  }

  def findNewAlbums: Observable[(NewAlbum, ReconID)] = {
    val cache = ArtistLastYearCache from getExistingAlbums
    Observable.from(cache.artists)
        .flatMap(Observable from findNewAlbums(cache, _))
        .flatMap(Observable from _)
  }

  private def findNewAlbums(cache: ArtistLastYearCache, artist: Artist): Future[Seq[(NewAlbum, ReconID)]] = {
    // TODO replace filter with Message with a sum type for better error messages
    reconciler(artist)
        .filterWithMessage(!_._2, "Ignored")
        .filterWithMessage(_._1.isDefined, s"Unreconcilable")
        .map(_._1.get)
        .toTry.get match { // Ensures a single thread waits for the semaphore in NewAlbumsConfig
      case Success(reconId) =>
        reconId.mapTo(meta.getAlbumsMetadata)
            .flatMap(removeIgnoredAlbums(artist, _))
            .map(cache.filterNewAlbums(artist, _))
            .listen(albums => {
              log(s"Finished working on $artist; found ${if (albums.isEmpty) "no" else albums.size} new albums.")
            })
            .listenError {
              case e: FilteredException => log(s"$artist was filtered, reason: ${e.getMessage}")
              case e: Throwable => e.printStackTrace()
            }.orElse(Nil)
      case Failure(e) =>
        logger.warn(s"Did not fetch albums for artist<${artist.name}>; reason: ${e.getMessage}")
        Future successful Nil
    }
  }
}

private object NewAlbumsRetriever {
  private def dirToAlbum(dir: IODirectory)(implicit c: RealConfig): Option[Album] = {
    val mf = c.injector.instance[IOMusicFinder]
    dir.files
        .find(_.extension |> mf.extensions)
        .map(_.file)
        .map(Song(_).release)
  }

  def main(args: Array[String]): Unit = {
    implicit val c: RealConfig = StandaloneConfig
    val mf = c.injector.instance[IOMusicFinder]

    val artist: Artist = Artist("At the Gates")
    def cacheForArtist(a: Artist)(implicit c: RealConfig): ArtistLastYearCache = {
      val mf = c.injector.instance[IOMusicFinder]
      val artistDir = mf.genreDirs
          .flatMap(_.deepDirs)
          .find(_.name.toLowerCase == a.name.toLowerCase)
          .get
      val lastAlbum = artistDir.dirs.mapIf(_.isEmpty).to(Seq(artistDir))
          .flatMap(dirToAlbum(_))
          .maxBy(_.year)

      Seq.apply(lastAlbum) |> ArtistLastYearCache.from
    }
    def findNewAlbums(a: Artist): Future[Seq[NewAlbum]] =
      new NewAlbumsRetriever(
        new ReconcilerCacher(new ArtistReconStorage(), new MbArtistReconciler()),
        new AlbumReconStorage(),
        mf,
      ).findNewAlbums(cacheForArtist(a), artist)
          .map(_.map(_._1))
    findNewAlbums(artist).get.log()
    Thread.getAllStackTraces.keySet.asScala.filterNot(_.isDaemon).foreach(println)
  }
}
