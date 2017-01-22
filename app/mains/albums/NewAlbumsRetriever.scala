package mains.albums

import backend.mb.MbArtistReconciler
import backend.recon.Reconcilable.SongExtractor
import backend.recon._
import common.io.IOFile
import common.rich.RichFuture._
import common.rich.RichT._
import models.{MusicFinder, Song}
import org.joda.time.LocalDate
import rx.lang.scala.Observable

import scala.concurrent.{ExecutionContext, Future}

private class NewAlbumsRetriever(reconciler: ReconcilerCacher[Artist], mf: MusicFinder)(implicit ec: ExecutionContext) {
  val meta = new MbArtistReconciler
  private var lastArtist: Option[Artist] = None
  private def getExistingAlbums: Iterator[Album] = mf.genreDirs
      .iterator
      .flatMap(_.deepDirs)
      .collect {
        case d => d.files.filter(f => mf.extensions.contains(f.extension))
      }.filter(_.nonEmpty)
      .map(files => Song(files.head.asInstanceOf[IOFile].file).release)
  private def getLastAlbum(e: (Artist, Seq[Album])) =
    (e._1, e._2.toVector.map(_.year).sorted.last)
  private def parseAlbum(artistName: String, e: (LocalDate, String)): Album =
    Album(e._2, e._1.getYear, Artist(artistName))
  //TODO return a Future of... something
  def findNewAlbums: Observable[Album] = {
    val lastReleaseYear: Map[Artist, Int] = getExistingAlbums
        .toSeq
        .groupBy(_.artist)
        .map(getLastAlbum)
    def isNewAlbum(e: Album): Boolean =
      if (lastReleaseYear(e.artist) < e.year)
        true
      else {
        for (a <- lastArtist)
          if (a != e.artist) println("Finished " + a)
        lastArtist = Some(e.artist)
        false
      }
    def extractNewAlbums(artist: Artist, albums: Seq[(LocalDate, String)]): Seq[Album] =
      albums.filter(_._1.toDateTimeAtCurrentTime.getMillis < System.currentTimeMillis())
          .map(e => Album(e._2, e._1.getYear, artist))
          .filter(isNewAlbum)
    Observable.from(lastReleaseYear.keys)
        .flatMap { artist =>
          print(s"Working on $artist... ")
          Observable.from(reconciler(artist)
              .filter { e =>
                if (e._2)
                  println("Ignoring " + artist)
                else if (e._1.isEmpty)
                  println("Could not reconcile " + artist)
                !e._2
              }
              .map(_._1.get)
              .flatMap(meta.getAlbumsMetadata)
              .map(extractNewAlbums(artist, _))
              .orElse(Nil))
        }.flatMap(Observable.from(_))
  }

  // for debugging
  //TODO incorporate
  def findNewAlbums(a: Artist): Future[Seq[Album]] = {
    reconciler(a).map(_._1.get).flatMap(meta.getAlbumsMetadata).map(_.map(parseAlbum(a.name, _)))
  }
}
