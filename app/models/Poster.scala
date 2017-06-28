package models

import java.io.File

import common.rich.path.Directory
import common.rich.path.RichFile._

import scala.annotation.tailrec

object Poster {
  @tailrec
  private def getCoverArt(dir: Directory): File =
    dir.files.find(_.name.toLowerCase.matches("folder.(jpg)|(png)")) match {
      case Some(f) => f
      case None => getCoverArt(dir.parent)
    }
  def getCoverArt(s: IOSong): File = getCoverArt(s.file.parent.dir)
}
