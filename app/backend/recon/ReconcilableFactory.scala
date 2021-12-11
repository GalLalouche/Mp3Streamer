package backend.recon

import backend.recon.Reconcilable.SongExtractor
import javax.inject.Inject
import models.MusicFinder

import common.io.DirectoryRef
import common.rich.RichT._
import common.rich.primitives.RichOption.richOption

class ReconcilableFactory @Inject()(mf: MusicFinder) {
  private val invalidDirectoryNames: Map[String, String] = Map(
    // Some artists have invalid directory characters in their name, so their directory won't match
    // the artist name. As a stupid hack, just aggregate them below.
    "ArchMatheos" -> "Arch / Matheos",
  )
  def dirNameToArtist(dirName: String): Artist =
    Artist(dirName optionOrKeep invalidDirectoryNames.get)
  def toAlbum(dir: DirectoryRef): Album =
    if (dir.name.take(4).forall(_.isDigit)) {
      val split = dir.name.split(" ", 2).ensuring(_.length == 2, s"Bad name for <$dir>")
      Album(
        title = split(1),
        // Some albums are prefixed with 1969A.
        year = split(0).ensuring(s => s.length == 4 || s.length == 5).take(4).toInt,
        artist = dirNameToArtist(dir.parent.name),
      )
    } else // Single album artist.
      mf.parseSong(mf.getSongFilesInDir(dir).headOption.getOrThrow(s"Problem with $dir")).release

  private val IgnoredFolders = Vector("Classical", "Musicals")
  def artistDirectories: Seq[DirectoryRef] = {
    val prefixLength = {
      val $ = mf.dir.path
      $.length + (if ($.endsWith("\\") || $.endsWith("/")) 0 else 1)
    }
    def ignore(dir: DirectoryRef): Boolean = {
      val genrePrefix = dir.path.drop(prefixLength)
      IgnoredFolders.exists(genrePrefix.startsWith)
    }
    mf.artistDirs.filterNot(ignore)
  }
  def albumDirectories: Seq[DirectoryRef] =
    mf.albumDirs(artistDirectories.asInstanceOf[Seq[this.mf.S#D]])
}
