package mains.random_folder

import java.io.File

import javax.inject.Inject
import models.MusicFinder

import common.rich.path.Directory
import common.rich.path.RichFile._
import common.rich.primitives.RichBoolean._

// TODO some day might use MusicFinder
private class SongDataExtractor @Inject()(mf: MusicFinder) {
  private def go(artistDir: Directory, album: String) = {
    val artistName = artistDir.name
    val genreDir = artistDir.parent
    val genreName = genreDir.name
    SongData(genre = genreName.toLowerCase, artist = artistName.toLowerCase, album = album.toLowerCase)
  }
  def apply(f: File): SongData = {
    val albumDir = f.parent
    val albumName = albumDir.name
    // Single artist dirs
    // TODO extract this logic to somewhere else
    if (albumName.take(4).forall(_.isDigit).isFalse)
      go(albumDir, "Single-artist-dir")
    else
      go(albumDir.parent, albumName)
  }
}