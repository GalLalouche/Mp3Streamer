package mains

import java.nio.file.Files
import java.nio.file.attribute.FileTime

import models.{IOMusicFinder, IOSong}

import common.io.IODirectory
import common.rich.RichT._
import common.rich.path.RichFileUtils

private object ArtistNameAdder {
  def main(args: Array[String]): Unit = {
    val mf = IOMusicFinder
    def go(dir: IODirectory): Unit =
      try {
        val artistName = IOSong.read(mf.getSongFilesInDir(dir).head.file).artistName
        val originalTime = FileTime.fromMillis(dir.file.lastModified)
        if (dir.name.toLowerCase contains artistName.toLowerCase)
          return
        val newName = s"$artistName - ${dir.name}"
        println(s"Renaming <$dir> to <$newName>")
        RichFileUtils.rename(dir.dir, newName)
            .dir.toPath <| (Files.setLastModifiedTime(_, originalTime))
      } catch {
        case e: Exception => println(s"Error in <$dir>: ${e.getMessage}")
      }
    IODirectory("""E:\Incoming\Soulseek\complete""").dirs foreach go
  }
}
