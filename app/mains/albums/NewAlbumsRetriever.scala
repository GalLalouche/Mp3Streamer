package mains.albums

import common.io.IOFileRef
import models.{MusicFinder, Song}

private class NewAlbumsRetriever(meta: MetadataRetriever, music: MusicFinder, ignoredArtists: Seq[String]) {
  var lastArtist: Option[String] = None
  def getAlbums: Iterator[Album] = music.genreDirs
    .iterator
    .flatMap(_.deepDirs)
    .collect {
      case d => d.files.filter(f => music.extensions.contains(f.extension))
    }.filter(_.nonEmpty)
    .map(files => Album(Song(files.head.asInstanceOf[IOFileRef].file)))
  def getLastAlbum(e: (String, Seq[Album])) = e._1.toLowerCase -> e._2.toVector
    .sortBy(_.year)
    .map(_.year)
    .last
  def findNewAlbums: Iterator[Album] = {
    val lastAlbumsByArtist = getAlbums
      .toSeq
      .groupBy(_.artist.toLowerCase)
      .map(getLastAlbum)
    def isNewAlbum(e: Album): Boolean = // assumes they are sorted by year... perhaps it shouldn't :|
      if (lastAlbumsByArtist(e.artist.toLowerCase) < e.year)
        true
      else {
        for (a <- lastArtist)
          if (a != e.artist) println("Finished " + a)
        lastArtist = Some(e.artist)
        false
      }
    lastAlbumsByArtist.keys.iterator
      .filterNot(ignoredArtists.contains)
      .flatMap(meta.getAlbums(_)) // has default argument, can't bind by name
      .filter(isNewAlbum)
  }
}
