package mains.random_folder

import java.io.File

import backend.recon.Artist
import com.google.inject.Inject
import genre.GenreFinder
import org.typelevel.ci.CIString

import common.path.ref.io.IODirectory
import common.rich.RichFile._

private class SongDataExtractor @Inject() (genreFinder: GenreFinder) {
  private def go(artistDir: IODirectory, album: String) = SongData(
    genre = genreFinder(artistDir),
    artist = Artist(artistDir.name),
    album = CIString(album),
  )
  def apply(f: File): SongData = {
    val albumDir = f.parent
    val albumName = albumDir.name
    // Single artist dirs
    // TODO extract this logic to somewhere else
    if (albumName.take(4).forall(_.isDigit))
      go(albumDir.parent, albumName)
    else
      go(albumDir, "Single-artist-dir")
  }
}
