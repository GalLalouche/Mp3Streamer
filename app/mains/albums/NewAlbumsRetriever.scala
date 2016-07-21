package mains.albums

import backend.mb.MbArtistReconciler
import backend.recon.{Artist, ArtistReconcilerCacher}
import common.RichFuture._
import common.io.IOFile
import common.rich.RichT._
import models.{MusicFinder, Song}

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
    val lastAlbumsByArtist = getExistingAlbums
        .toSeq
        .groupBy(_.artist.toLowerCase)
        .map(getLastAlbum)
    def isNewAlbum(e: Album): Boolean =
      if (lastAlbumsByArtist(e.artist.toLowerCase) < e.year)
        true
      else {
        for (a <- lastArtist)
          if (a != e.artist) println("Finished " + a)
        lastArtist = Some(e.artist)
        false
      }
    val $: Iterator[Iterator[Album]] = for (artist <- lastAlbumsByArtist.keys.iterator) yield {
      print("Working on artist: " + artist + "... ")
      val f = reconciler.apply(artist |> Artist)
          .filter(_._2 == false)
          .get
          .log()
      Iterator.empty
//          .map(_._1)
//          .map(_.map(meta.getAlbumsMetadata)
//              .map(_.map(_.filter(_._1.toDateTimeAtCurrentTime.getMillis < System.currentTimeMillis()))
//                  .map(_.map(e => Album(artist, e._1.getYear, e._2)))
//                  .map(_ filter isNewAlbum)
//                  .recover {case e => Seq()}
//                  .get
//                  .iterator)
//              .getOrElse {
//                println("Ignoring... ")
//                Iterator.empty
//              })
//      Try(f.get).getOrElse(Iterator.empty)
    }
    $.flatten
  }
}
