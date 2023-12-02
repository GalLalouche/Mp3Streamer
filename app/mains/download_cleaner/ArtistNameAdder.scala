package mains.download_cleaner

import java.nio.file.attribute.FileTime
import java.nio.file.Files
import javax.inject.Inject

import common.io.IODirectory
import common.rich.path.RichFileUtils
import common.rich.primitives.RichBoolean.richBoolean
import common.rich.RichT.richT
import models.{IOMusicFinder, SongTagParser}

/** Adds the artist name to the folder if it doesn't contain it already. */
private class ArtistNameAdder @Inject() (mf: IOMusicFinder) extends Cleaner {
  override def apply(dir: IODirectory): Unit = dir.dirs.foreach(go)
  private def go(dir: IODirectory): Unit =
    try {
      val song = SongTagParser.optionalSong(mf.getSongFilesInDir(dir).head.file)
      val yearOption: Option[Int] = song.year
      val artistName = song.artistName.get
      val originalTime = FileTime.fromMillis(dir.file.lastModified)
      val dirName = dir.name
      val needsArtist = dirName.toLowerCase.contains(artistName.toLowerCase).isFalse
      lazy val year = yearOption.get.toString
      val needsYear = yearOption.isDefined && dirName.contains(year).isFalse
      val newName = (needsArtist, needsYear) match {
        case (false, false) => return
        case (true, true) => s"$artistName - $year - $dirName"
        case (true, false) => s"$artistName - $dirName"
        case (false, true) => s"$year - $dirName"
      }
      assert(newName.contains("Some(").isFalse)
      assert(newName != dirName)
      println(s"Renaming <$dirName> to <$newName>")
      RichFileUtils.rename(dir.dir, newName).toPath.<|(Files.setLastModifiedTime(_, originalTime))
    } catch {
      case e: Exception => println(s"Error in <$dir>: ${e.getMessage}", e)
    }
}
