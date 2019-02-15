package mains.fixer

import java.io.File
import java.util.logging.{Level, Logger}

import common.rich.RichT.richT
import common.rich.collections.RichTraversableOnce._
import common.rich.path.Directory
import common.rich.path.RichFile.richFile
import common.rich.path.RichPath.poorPath
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichOption._
import common.rich.primitives.RichString.richString
import models.{IOSong, Song}
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.{FieldKey, Tag}
import org.jaudiotagger.tag.flac.FlacTag
import org.jaudiotagger.tag.id3.ID3v24Tag

import scala.annotation.tailrec
import scala.util.Try

/** Fixes ID3 tags on mp3 (and flac) files to proper casing, etc. */
private object FixLabels {
  Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF)
  private def properTrackString(track: Int): String = if (track < 10) "0" + track else track.toString
  private[fixer] def fixTag(f: File, fixDiscNumber: Boolean): Tag = {
    val audioFile = AudioFileIO read f
    val originalTag = audioFile.getTag
    val newTag = if (f.extension.toLowerCase == "flac") new FlacTag else new ID3v24Tag

    Seq(FieldKey.ARTIST, FieldKey.TITLE, FieldKey.ALBUM, FieldKey.YEAR)
        .foreach(f => newTag.setField(f, originalTag getFirst f mapTo StringFixer.apply))
    newTag.setField(FieldKey.TRACK, originalTag.getFirst(FieldKey.TRACK).toInt mapTo properTrackString)
    // Not all track need to have a disc number property, e.g., bonus track
    if (fixDiscNumber && originalTag.hasField(FieldKey.DISC_NO))
      newTag.setField(FieldKey.DISC_NO, originalTag
          .getFirst(FieldKey.DISC_NO)
          .mapIf(_ matches "\\d+[/\\\\].*").to(_ takeWhile (_.isDigit)))

    newTag
  }

  private def fixFile(f: File, fixDiscNumber: Boolean): Unit = {
    val audioFile = AudioFileIO read f
    val newTag = fixTag(f, fixDiscNumber)
    AudioFileIO delete audioFile
    audioFile setTag newTag
    audioFile.commit()
  }

  private def properFileName(f: File): String =
    IOSong.read(f) mapTo (song => s"${properTrackString(song.track)} - ${song.title}.${f.extension}")

  private def rename(f: File): Unit = {
    f renameTo new File(f.parent, properFileName(f))
  }

  private def retrieveYear(song: Song): Int =
    Try(song.year)
        .orElse(Try(song.file.parent.name.captureWith("\\D*([12]\\d{3})\\D*".r).toInt)) // Y3/0K Bug
        .toOption
        .getOrThrow(new Exception("Could not retrieve the year from the songs"))

  private val reservedDirCharacters = "<>:\"/\\|?*".toSet

  @tailrec
  private def renameFolder(source: Directory, initialName: String): Directory = {
    val file = new File(source.parent, initialName)
    if (source.dir renameTo file) Directory(file) else renameFolder(source, initialName + "_temp")
  }

  def fix(dir: Directory): FixedDirectory = {
    def containsASingleFileWithExtension(extension: String) = dir.files.count(_.extension == extension) == 1
    require((containsASingleFileWithExtension("flac") && containsASingleFileWithExtension("cue")).isFalse,
      "Folder contains an unsplit flac file; please split the file and try again.")

    dir.files.foreach(_ setWritable true) // stupid bittorrent
    dir.files.filter(_.extension == "m3u").foreach(_.delete)

    val musicFiles = dir.files.filter(_.extension |> Set("mp3", "flac"))
    require(musicFiles.nonEmpty, s"Could not find any songs in $dir - maybe they're in subfolders...")

    // as opposed to 1/1 - Fuck those guys.
    val hasNonTrivialDiscNumber = musicFiles
        .map(AudioFileIO.read)
        .hasSameValues(_.getTag getFirst FieldKey.DISC_NO)
        .isFalse

    val (year, album) = musicFiles.head.mapTo(IOSong.read)
        .mapTo(firstSong => retrieveYear(firstSong) -> StringFixer(firstSong.albumName))

    musicFiles foreach (fixFile(_, hasNonTrivialDiscNumber))
    musicFiles foreach rename

    val expectedName = s"$year ${album filterNot reservedDirCharacters}"
    try
      new FixedDirectory(renameFolder(dir, expectedName), expectedName)
    catch {
      case e: Exception => throw new Exception("could not rename the folder", e)
    }
  }

  // Sometimes renaming FLAC files fails, but like, only for some files?!
  def verify(dir: Directory): Boolean =
    dir.files.filter(_.extension |> Set("mp3", "flac")) forall (f => f.name == properFileName(f))
}
