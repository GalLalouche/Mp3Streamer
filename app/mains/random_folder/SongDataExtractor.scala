package mains.random_folder

import java.io.File
import javax.inject.Inject

import backend.recon.Artist
import genre.GenreFinder
import mains.random_folder

import common.io.IODirectory
import common.rich.path.Directory
import common.rich.path.RichFile._
import common.rich.primitives.RichBoolean._

private class SongDataExtractor @Inject() (genreFinder: GenreFinder) {
  private def go(artistDir: Directory, album: String) = random_folder.SongData(
    genre = genreFinder(IODirectory(artistDir)),
    artist = Artist(artistDir.name),
    album = album.toLowerCase,
  )
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
