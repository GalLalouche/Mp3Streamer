package backend.albums.filler

import backend.recon.{Album, Artist}
import backend.recon.Reconcilable.SongExtractor
import models.MusicFinder

import common.io.DirectoryRef
import common.rich.RichT._

private trait ExistingAlbums {
  def artists: Iterable[Artist]
  def albums: Artist => Set[Album]
}

private object ExistingAlbums {
  private val invalidDirectoryNames: Map[String, String] = Map(
    // Some artists have invalid directory characters in their name, so their directory won't match
    // the artist name. As a stupid hack, just aggregate them below.
    "ArchMatheos" -> "Arch / Matheos",
  )
  def dirNameToArtist(dirName: String): Artist =
    Artist(dirName optionOrKeep invalidDirectoryNames.get)
  def toAlbum(mf: MusicFinder)(dir: DirectoryRef): Album =
    if (dir.name.take(4).forall(_.isDigit)) {
      val split = dir.name.split(" ", 2).ensuring(_.length == 2)
      Album(
        title = split(1),
        // Some albums are prefixed with 1969A.
        year = split(0).ensuring(s => s.length == 4 || s.length == 5).take(4).toInt,
        artist = dirNameToArtist(dir.parent.name),
      )
    } else
      mf.parseSong(mf.getSongFilesInDir(dir).head).release // Single album artist.
}
