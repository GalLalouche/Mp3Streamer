package models

import java.io.File
import java.util.regex.Pattern

import common.rich.path.Directory
import common.rich.path.RichFile._
import common.rich.primitives.RichString._

object Poster {
  private val FolderImagePattern = Pattern.compile("folder\\.(jpg)|(png)", Pattern.CASE_INSENSITIVE)
  /** Searches for a folder image recursively up the folder tree. */
  private def getCoverArt(dir: Directory): File =
    dir.files.find(_.name.matches(FolderImagePattern)).getOrElse(getCoverArt(dir.parent))
  def getCoverArt(s: IOSong): File = getCoverArt(s.file.parent.dir)
}
