package mains

import java.nio.file.Files
import java.nio.file.attribute.FileTime

import models.{IOMusicFinder, IOSong}

import common.io.IODirectory
import common.rich.RichT._
import common.rich.path.RichFileUtils
import common.rich.primitives.RichBoolean.richBoolean

private object ArtistNameAdder {
  def main(args: Array[String]): Unit = {
    val folder = IODirectory("""E:\Incoming\Soulseek\complete""")
    val mf = IOMusicFinder
    for (dir <- folder.dirs)
      try {
        val artistName = IOSong.read(mf.getSongFilesInDir(dir).head.file).artistName
        val originalTime = FileTime.fromMillis(dir.file.lastModified)
        if (dir.name.toLowerCase.contains(artistName.toLowerCase).isFalse) {
          val newName = s"$artistName - ${dir.name}"
          println(s"Renaming <$dir> to <$newName>")
          RichFileUtils.rename(dir.dir, newName)
              .dir
              .|>(e => Files.setLastModifiedTime(e.toPath, originalTime))
        }
      } catch {
        case e: Exception => println(s"Error in <$dir>: ${e.getMessage}")
      }
  }
}
