package mains.quick_fixers

import java.nio.file.FileAlreadyExistsException

import common.rich.path.{Directory, RichFileUtils}
import common.rich.path.RichFile._
import common.rich.primitives.RichString._

/** Renames stupid X.flac.cue files to X.cue files. */
private object FixFlacCueFiles {
  def go(dir: String): Unit =
    Directory(dir).deepFiles.filter(_.name.endsWith(".flac.cue")).foreach { file =>
      println(s"Renaming <$file>")
      try
        RichFileUtils.rename(file, file.getName.simpleReplace(".flac.cue", ".cue"))
      catch {
        case _: FileAlreadyExistsException => println("Replacement cue already exists, skipping")
      }
    }
}
