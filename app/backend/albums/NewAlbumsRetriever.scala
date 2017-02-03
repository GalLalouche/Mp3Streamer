package backend.albums

import backend.configs.Configuration
import backend.mb.MbArtistReconciler
import backend.mb.MbArtistReconciler.MbAlbumMetadata
import backend.recon.Reconcilable.SongExtractor
import backend.recon._
import common.io.IOFile
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.func.MoreMonadPlus._
import models.{MusicFinder, Song}
import org.joda.time.Duration
import rx.lang.scala.Observable

import scala.concurrent.Future
import scalaz.std.{FutureInstances, ListInstances}
import scalaz.syntax.ToTraverseOps

private class NewAlbumsRetriever(reconciler: ReconcilerCacher[Artist], albumReconStorage: AlbumReconStorage)(
    implicit c: Configuration, mf: MusicFinder)
    extends FutureInstances with ToTraverseOps with ListInstances {
  private val log = c.logger.verbose _
  private val meta = new MbArtistReconciler
  private def getExistingAlbums: Seq[Album] = mf.genreDirs
      .flatMap(_.deepDirs)
      .flatMap(_.files
          .find(f => f.extension |> mf.extensions)
          .map(_.asInstanceOf[IOFile].file)
          .map(Song(_).release))
  private def removeIgnored(artist: Artist, as: Seq[MbAlbumMetadata]): Future[Seq[MbAlbumMetadata]] = {
    def isIgnored($: Option[(_, Boolean)]) = $.exists(_._2)
    def toAlbum(album: MbAlbumMetadata): Album =
      Album(title = album.title, year = album.releaseDate.getYear, artist = artist)
    val $ = as.fproduct(toAlbum(_) |> albumReconStorage.load).map(e => e._2.fproduct(e._1.const))
        .toList // TODO make Seq Traversable
        .sequenceU
        .map(_.filterNot(e => isIgnored(e._1)).map(_._2))
    $
  }
  def findNewAlbums: Observable[(NewAlbum, ReconID)] = {
    val cache = ArtistLastYearCache.from(getExistingAlbums)
    Observable.from(cache.artists)
        .flatMap {artist =>
          val start = System.currentTimeMillis()
          log(s"Working on $artist...")
          Observable.from(reconciler(artist)
              .filterWith(!_._2, "Ignored")
              .filterWith(_._1.isDefined, s"Unreconcilable")
              .map(_._1.get)
              .flatMap(meta.getAlbumsMetadata)
              .flatMap(removeIgnored(artist, _))
              .map(cache.filterNewAlbums(artist, _))
              .consume(e => {
                val totalTime = Duration.millis(System.currentTimeMillis - start)
                log(s"Finished working on $artist; Found ${e.size} new albums; Took $totalTime.")
              })
              .recover {
                case e: NoSuchElementException =>
                  log(s"$artist was filtered, reason: ${e.getMessage}")
                  Nil
                case e: Throwable =>
                  e.printStackTrace()
                  Nil
              })
        }.flatMap(Observable.from(_))
  }
}
