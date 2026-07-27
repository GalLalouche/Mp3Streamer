package mains.random_folder

import java.io.File

import backend.recon.{Artist, ReconcilableFactory}
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
    if (ReconcilableFactory.hasYearPrefix(albumDir))
      go(albumDir.parent, albumDir.name)
    else
      go(albumDir, "Single-artist-dir")
  }
}
