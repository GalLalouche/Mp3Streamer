package backend.albums

import backend.logging.Logger
import backend.mb.MbArtistReconciler
import backend.mb.MbArtistReconciler.MbAlbumMetadata
import backend.recon._
import backend.recon.Reconcilable.SongExtractor
import com.google.inject.assistedinject.Assisted
import common.io.IODirectory
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.func.{MoreSeqInstances, MoreTraverseInstances, ToMoreFunctorOps, ToMoreMonadErrorOps, ToTraverseMonadPlusOps}
import common.rich.primitives.RichBoolean._
import javax.inject.Inject
import models.{IOMusicFinder, Song}
import rx.lang.scala.Observable

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import scalaz.Kleisli
import scalaz.std.FutureInstances

private class NewAlbumsRetriever @Inject()(
    albumReconStorage: AlbumReconStorage,
    logger: Logger,
    ec: ExecutionContext,
    meta: MbArtistReconciler,
    @Assisted mf: IOMusicFinder,
    @Assisted reconciler: ReconcilerCacher[Artist],
) extends FutureInstances with MoreTraverseInstances with ToMoreFunctorOps
    with ToTraverseMonadPlusOps with ToMoreMonadErrorOps with MoreSeqInstances {
  private implicit val iec: ExecutionContext = ec
  private val log = logger.verbose _
  private def getExistingAlbums: Seq[Album] = mf.genreDirs
      .flatMap(_.deepDirs)
      .flatMap(NewAlbumsRetriever.dirToAlbum(_, mf))
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
        .filterWithMessage(_._2.isFalse, "Ignored")
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
  import backend.configs.{RealConfig, StandaloneConfig}
  import net.codingwell.scalaguice.InjectorExtensions._

  import scala.collection.JavaConverters._

  private def dirToAlbum(dir: IODirectory, mf: IOMusicFinder): Option[Album] = dir.files
      .find(_.extension |> mf.extensions)
      .map(_.file)
      .map(Song(_).release)

  def main(args: Array[String]): Unit = {
    implicit val c: RealConfig = NewAlbumsConfig
    val injector = c.injector
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val mf = injector.instance[IOMusicFinder]

    val artist: Artist = Artist("At the Gates")
    def cacheForArtist(a: Artist, mf: IOMusicFinder): ArtistLastYearCache = {
      val mf = c.injector.instance[IOMusicFinder]
      val artistDir = mf.genreDirs
          .flatMap(_.deepDirs)
          .find(_.name.toLowerCase == a.name.toLowerCase)
          .get
      val lastAlbum = artistDir.dirs.mapIf(_.isEmpty).to(Seq(artistDir))
          .flatMap(dirToAlbum(_, mf))
          .maxBy(_.year)

      Seq.apply(lastAlbum) |> ArtistLastYearCache.from
    }
    val reconciler = new ReconcilerCacher(
      injector.instance[ArtistReconStorage], injector.instance[MbArtistReconciler])
    val $ = c.injector.instance[NewAlbumsRetrieverFactory].apply(reconciler, mf)
    $.findNewAlbums(cacheForArtist(artist, mf), artist).map(_.map(_._1)).get.log()
    Thread.getAllStackTraces.keySet.asScala.filterNot(_.isDaemon).foreach(println)
  }
}
