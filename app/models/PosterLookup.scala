package models

import java.util.regex.Pattern

import common.io.{DirectoryRef, FileRef}
import common.rich.primitives.RichString._

trait PosterLookup {
  def getCoverArt(s: Song): FileRef
}

object PosterLookup {
  object IOPosterLookup extends PosterLookup {
    override def getCoverArt(s: Song): FileRef = getCoverArt(s.asInstanceOf[IOSong].file.parent)

    // Searches for a folder image recursively up the folder tree.
    private def getCoverArt(dir: DirectoryRef): FileRef =
      dir.files.find(_.name.matches(FolderImagePattern)).getOrElse(getCoverArt(dir.parent))

    private val FolderImagePattern =
      Pattern.compile("folder\\.(jpg)|(png)", Pattern.CASE_INSENSITIVE)
  }
}
