package mains.download_cleaner

import java.nio.file.attribute.FileTime
import java.nio.file.Files

import javax.inject.Inject
import models.{IOMusicFinder, IOSong}

import common.io.IODirectory
import common.rich.path.RichFileUtils
import common.rich.RichT._

/** Adds the artist name to the folder if it doesn't contain it already. */
private class ArtistNameAdder @Inject() (mf: IOMusicFinder) extends Cleaner {
  override def apply(dir: IODirectory): Unit = dir.dirs.foreach(go)
  private def go(dir: IODirectory): Unit =
    try {
      val artistName = IOSong.read(mf.getSongFilesInDir(dir).head.file).artistName
      val originalTime = FileTime.fromMillis(dir.file.lastModified)
      if (dir.name.toLowerCase contains artistName.toLowerCase)
        return
      val newName = s"$artistName - ${dir.name}"
      println(s"Renaming <$dir> to <$newName>")
      RichFileUtils.rename(dir.dir, newName).dir.toPath <| (Files
        .setLastModifiedTime(_, originalTime))
    } catch {
      case e: Exception => println(s"Error in <$dir>: ${e.getMessage}")
    }
}
