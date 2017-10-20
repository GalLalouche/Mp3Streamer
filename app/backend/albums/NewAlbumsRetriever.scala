package backend.albums

import java.time.Duration

import backend.configs.{Configuration, StandaloneConfig}
import backend.mb.MbArtistReconciler
import backend.mb.MbArtistReconciler.MbAlbumMetadata
import backend.recon.Reconcilable.SongExtractor
import backend.recon._
import common.io.IODirectory
import common.rich.RichFuture
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.func.{MoreFutureInstances, MoreSeqInstances}
import models.{IOMusicFinder, Song}
import rx.lang.scala.Observable

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scalaz.std.ListInstances
import scalaz.syntax.ToTraverseOps

private class NewAlbumsRetriever(reconciler: ReconcilerCacher[Artist], albumReconStorage: AlbumReconStorage)(
    implicit c: Configuration, mf: IOMusicFinder)
    extends ToTraverseOps with ListInstances with MoreFutureInstances with MoreSeqInstances {
  private val log = c.logger.verbose _
  private val meta = new MbArtistReconciler
  private def getExistingAlbums: Seq[Album] = mf.genreDirs
      .flatMap(_.deepDirs)
      .flatMap(NewAlbumsRetriever.dirToAlbum(_))
  private def removeIgnoredAlbums(artist: Artist, as: Seq[MbAlbumMetadata]): Future[Seq[MbAlbumMetadata]] = {
    def toAlbum(album: MbAlbumMetadata): Album =
      Album(title = album.title, year = album.releaseDate.getYear, artist = artist)
    // TODO generalize to RichFuture: should be filterable with a Future[Boolean]
    as.map(e => albumReconStorage isIgnored toAlbum(e) strengthL e)
        .sequenceU
        .map(_.filterNot(_._2 getOrElse false).map(_._1))
  }

  def findNewAlbums: Observable[(NewAlbum, ReconID)] = {
    val cache = ArtistLastYearCache from getExistingAlbums
    Observable.from(cache.artists)
        .flatMap(Observable from findNewAlbums(cache, _))
        .flatMap(Observable from _)
  }

  private def findNewAlbums(cache: ArtistLastYearCache, artist: Artist): Future[Seq[(NewAlbum, ReconID)]] = {
    val start = System.currentTimeMillis()
    reconciler(artist)
        .filterWith(!_._2, "Ignored")
        .filterWith(_._1.isDefined, s"Unreconcilable")
        .map(_._1.get)
        .toTry.get match { // Ensures a single thread waits for the semaphore in NewAlbumsConfig
      case Success(reconId) =>
        reconId.mapTo(meta.getAlbumsMetadata)
            .flatMap(removeIgnoredAlbums(artist, _))
            .map(cache.filterNewAlbums(artist, _))
            .consume(e => {
              val totalTime = Duration.ofMillis(System.currentTimeMillis - start)
              val newAlbumsCount = if (e.isEmpty) "no" else e.size
              log(s"Finished working on $artist; found ${newAlbumsCount.toString.mapIf(_ == "0") to "no"} new albums.")
            })
            .recover {
              case e: Throwable =>
                e match {
                  case e: RichFuture.FilteredException => log(s"$artist was filtered, reason: ${e.getMessage}")
                  case e: Throwable => e.printStackTrace()
                }
                Nil
            }
      case Failure(e) => Future.successful(Nil)
    }
  }
}

object NewAlbumsRetriever {
  private def dirToAlbum(dir: IODirectory)(implicit mf: IOMusicFinder): Option[Album] = dir.files
      .find(_.extension |> mf.extensions)
      .map(_.file)
      .map(Song(_).release)

  def main(args: Array[String]): Unit = {
    implicit val c = StandaloneConfig
    import c._
    val artist: Artist = Artist("At the Gates")
    def cacheForArtist(a: Artist)(implicit mf: IOMusicFinder): ArtistLastYearCache = {
      val artistDir = mf.genreDirs
          .flatMap(_.deepDirs)
          .find(_.name.toLowerCase == a.name.toLowerCase)
          .get
      val lastAlbum = artistDir.dirs.mapIf(_.isEmpty).to(Seq(artistDir))
          .flatMap(dirToAlbum(_))
          .maxBy(_.year)

      Seq.apply(lastAlbum) |> ArtistLastYearCache.from
    }
    def findNewAlbums(a: Artist): Future[Seq[NewAlbum]] = {
      val artistReconciler: ReconcilerCacher[Artist] =
        new ReconcilerCacher(new ArtistReconStorage(), new MbArtistReconciler())
      val $ = new NewAlbumsRetriever(artistReconciler, new AlbumReconStorage())(c, c.mf)
      $.findNewAlbums(cacheForArtist(a), artist).map(_.map(_._1))
    }
    findNewAlbums(artist).get.log()
    Thread.getAllStackTraces.keySet.asScala.filterNot(_.isDaemon).foreach(println)
  }
}
