package mains.quick_fixers

import java.nio.file.FileAlreadyExistsException

import common.rich.path.{Directory, RichFileUtils}
import common.rich.path.RichFile._

/** Renames stupid X.flac.cue files to X.cue files. */
private object FixFlacCueFiles {
  def main(args: Array[String]): Unit = {
    val dir = Directory("""E:\Incoming\Bittorrent\Completed\Music""")
    dir.deepFiles.filter(_.name.endsWith(".flac.cue")) foreach {file =>
      println(s"Renaming <$file>")
      try {
        RichFileUtils.rename(file, file.getName.replaceAll(".flac.cue", ".cue"))
      } catch {
        case _: FileAlreadyExistsException => println("Replacement cue already exists, skipping")
      }
    }
  }
}
