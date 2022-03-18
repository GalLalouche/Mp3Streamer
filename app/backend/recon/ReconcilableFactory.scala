package backend.recon

import backend.recon.Reconcilable.SongExtractor
import javax.inject.Inject
import models.MusicFinder

import scala.util.{Failure, Success, Try}

import common.io.DirectoryRef
import common.rich.RichT._
import common.rich.primitives.RichOption.richOption

class ReconcilableFactory @Inject()(val mf: MusicFinder) {
  type S = mf.S
  private val invalidDirectoryNames: Map[String, String] = Map(
    // Some artists have invalid directory characters in their name, so their directory won't match
    // the artist name. As a stupid hack, just aggregate them below.
    // Surely a coincidence they all start with A o_O
    "ArchMatheos" -> "Arch / Matheos",
    "AndersonStolt" -> "Anderson/Stolt",
    "ACDC" -> "AC/DC",
  )
  def dirNameToArtist(dirName: String): Artist =
    Artist(dirName optionOrKeep invalidDirectoryNames.get)
  // This is Try so the error could be reserved.
  def toAlbum(dir: DirectoryRef): Try[Album] =
    if (dir.name.take(4).exists(_.isDigit)) {
      dir.name.split(" ", 2) match {
        case Array(yearStr, title) => Success(Album(
          title = title,
          // Some album years are suffixed with an ordering, e.g., 1969A.
          year = yearStr.ensuring(
            s => s.length == 4 || s.length == 5,
            s"<$yearStr> has weird format for <$dir>",
          ).take(4).toInt,
          artist = dirNameToArtist(dir.parent.name),
        ))
        case _ => Failure(new IllegalArgumentException(s"Bad name for <$dir>"))
      }
    } else
      Success(mf.parseSong(mf.getSongFilesInDir(dir).headOption.getOrThrow(s"Problem with $dir")).release)

  private val IgnoredFolders = Vector("Classical", "Musicals")
  def artistDirectories: Seq[S#D] = {
    val prefixLength = {
      val $ = mf.baseDir.path
      $.length + (if ($.endsWith("\\") || $.endsWith("/")) 0 else 1)
    }
    def ignore(dir: DirectoryRef): Boolean = {
      val genrePrefix = dir.path.drop(prefixLength)
      IgnoredFolders.exists(genrePrefix.startsWith)
    }
    mf.artistDirs.filterNot(ignore)
  }
  def albumDirectories: Seq[S#D] = mf.albumDirs(artistDirectories)
}
