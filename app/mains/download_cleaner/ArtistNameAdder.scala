package mains.download_cleaner

import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.FileTime

import com.google.inject.Inject
import mains.OptionalSongTagParser
import models.SongTagParser
import musicfinder.{ArtistNameNormalizer, IOMusicFinder}
import org.apache.commons.lang3.StringUtils.containsIgnoreCase

import common.io.IODirectory
import common.rich.RichT.richT
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.rich.path.RichFileUtils
import common.rich.primitives.RichBoolean.richBoolean

/** Adds the artist name to the folder if it doesn't contain it already. */
private class ArtistNameAdder @Inject() (
    mf: IOMusicFinder,
    artistNameNormalizer: ArtistNameNormalizer,
    songTagParser: SongTagParser,
) extends Cleaner {
  override def apply(dir: IODirectory): Unit = dir.dirs.foreach(go)
  private def go(dir: IODirectory): Unit = try {
    val song = OptionalSongTagParser(getSongFile(dir))
    val yearOption: Option[Int] = song.year
    val artistName = artistNameNormalizer(song.artistName.get)
    val originalTime = FileTime.fromMillis(dir.dir.lastModified)
    val dirName = dir.name
    val needsArtist = containsIgnoreCase(dirName, artistName).isFalse
    lazy val year = yearOption.get.toString
    val needsYear = yearOption.isDefined && dirName.contains(year).isFalse
    val newName = (needsArtist, needsYear) match {
      case (false, false) => return
      case (false, true) => s"$year - $dirName"
      case (true, false) => s"$artistName - $dirName"
      case (true, true) => s"$artistName - $year - $dirName"
    }
    assert(newName.contains("Some(").isFalse)
    assert(newName != dirName)
    println(s"Renaming <$dirName> to <$newName>")
    RichFileUtils.rename(dir.dir, newName).toPath.<|(Files.setLastModifiedTime(_, originalTime))
  } catch {
    case e: Exception => println(s"Error in <$dir>: ${e.getMessage}")
  }

  private def getSongFile(dir: IODirectory): File = {
    val songFiles = mf.getSongFilesInDir(dir)
    if (songFiles.isEmpty) {
      val nestedFiles = dir.dirs.flatMap(mf.getSongFilesInDir).toVector
      if (
        nestedFiles.nonEmpty &&
        nestedFiles.hasSameValues(songTagParser(_).toTuple(_.artistName, _.albumName))
      )
        nestedFiles.head.file
      else
        throw new NoSuchElementException(s"Could not extract a song from '$dir'")
    } else
      songFiles.next().file
  }
}
