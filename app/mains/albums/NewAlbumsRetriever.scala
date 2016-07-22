package mains.albums

import backend.mb.MbArtistReconciler
import backend.recon.{Artist, ArtistReconcilerCacher}
import common.RichFuture._
import common.io.IOFile
import common.rich.RichT._
import models.{MusicFinder, Song}
import org.joda.time.LocalDate

import scala.concurrent.ExecutionContext

private class NewAlbumsRetriever(reconciler: ArtistReconcilerCacher, mf: MusicFinder)(implicit ec: ExecutionContext) {
  val meta = new MbArtistReconciler
  private var lastArtist: Option[String] = None
  private def getExistingAlbums: Iterator[Album] = mf.genreDirs
      .iterator
      .flatMap(_.deepDirs)
      .collect {
        case d => d.files.filter(f => mf.extensions.contains(f.extension))
      }.filter(_.nonEmpty)
      .map(files => Album(Song(files.head.asInstanceOf[IOFile].file)))
  private def getLastAlbum(e: (String, Seq[Album])) =
    (e._1.toLowerCase, e._2.toVector.map(_.year).sorted.last)
  def findNewAlbums: Iterator[Album] = {
    val lastReleaseYear = getExistingAlbums
        .toSeq
        .groupBy(_.artist.toLowerCase)
        .map(getLastAlbum)
    def isNewAlbum(e: Album): Boolean =
      if (lastReleaseYear(e.artist.toLowerCase) < e.year)
        true
      else {
        for (a <- lastArtist)
          if (a != e.artist) println("Finished " + a)
        lastArtist = Some(e.artist)
        false
      }
    def extractNewAlbums(artistName: String, albums: Seq[(LocalDate, String)]): Seq[Album] =
      albums.filter(_._1.toDateTimeAtCurrentTime.getMillis < System.currentTimeMillis())
          .map(e => Album(artistName, e._1.getYear, e._2))
          .filter(isNewAlbum)
    lastReleaseYear.keys.iterator.map {artistName =>
      print("Working on artist: " + artistName + "... ")
      reconciler(artistName |> Artist)
          .filter {e =>
            if (e._2)
              println("Ignoring " + artistName)
            else if (e._1.isEmpty)
              println("Could not reconcile " + artistName)
            !e._2
          }
          .map(_._1.get)
          .flatMap(meta.getAlbumsMetadata)
          .map(extractNewAlbums(artistName, _))
          .orElse(Nil)
          .get
    }.flatten
  }
}
