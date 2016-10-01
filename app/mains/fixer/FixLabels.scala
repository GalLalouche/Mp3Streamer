package mains.fixer

import java.io.File
import java.util.logging.{Level, Logger}

import common.Debug
import common.rich.RichT.richT
import common.rich.collections.RichTraversableOnce._
import common.rich.path.Directory
import common.rich.path.RichFile.richFile
import common.rich.path.RichPath.poorPath
import common.rich.primitives.RichOption._
import common.rich.primitives.RichString.richString
import models.Song
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.flac.FlacTag
import org.jaudiotagger.tag.id3.ID3v24Tag
import org.jaudiotagger.tag.{FieldKey, Tag}

import scala.util.Try

/** Fixes ID3 tags on mp3 (and flac) files to proper casing, etc. */
object FixLabels extends Debug {
  Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF)
  private def properTrackString(track: Int): String = if (track < 10) "0" + track else track.toString
  private[fixer] def fixTag(f: File, fixDiscNumber: Boolean): Tag = {
    val audioFile = AudioFileIO read f
    val originalTag = audioFile.getTag
    val newTag = if (f.extension.toLowerCase == "flac") new FlacTag else new ID3v24Tag

    for (f <- List(FieldKey.ARTIST, FieldKey.TITLE, FieldKey.ALBUM, FieldKey.YEAR))
      newTag.setField(f, originalTag getFirst f mapTo StringFixer.apply)
    newTag.setField(FieldKey.TRACK, originalTag.getFirst(FieldKey.TRACK).toInt mapTo properTrackString)
    // Not all track need to have a disc number property, e.g., bonus track
    if (fixDiscNumber && originalTag.hasField(FieldKey.DISC_NO))
      newTag.setField(FieldKey.DISC_NO, originalTag
          .getFirst(FieldKey.DISC_NO)
          .mapIf(_ matches "\\d+[/\\\\].*").to(_ takeWhile (_.isDigit)))

    newTag
  }

  private def fixFile(f: File, fixDiscNumber: Boolean) {
    val audioFile = AudioFileIO read f
    val newTag = fixTag(f, fixDiscNumber)
    AudioFileIO delete audioFile
    audioFile setTag newTag
    audioFile.commit()
  }

  private def rename(f: File) {
    Song(f) mapTo (song =>
      f renameTo new File(f.parent, "%s - %s.%s".format(properTrackString(song.track), song.title, f.extension)))
  }

  private def retrieveYear(song: Song): Int =
    Try(song.year)
        .orElse(Try(song.file.parent.name.captureWith("\\D*([12]\\d{3})\\D*".r).toInt)) // Y3/0K Bug
        .toOption
        .getOrThrow(new Exception("Could not retrieve the year from the songs"))

  private val reservedDirCharacters = "<>:\"/\\|?*".toSet

  // returns the path of the output folder
  def fix(dir: Directory): Directory = {
    def containsASingleFileWithExtension(extension: String) = dir.files.count(_.extension == extension) == 1
    require(!(containsASingleFileWithExtension("flac") && containsASingleFileWithExtension("cue")),
      "Folder contains an unsplit flac file; please split the file and try again.")

    dir.files.foreach(_ setWritable true) // stupid bittorrent
    dir.files.filter(_.extension == "m3u").foreach(_.delete)

    val musicFiles = dir.files.filter(_.extension |> Set("mp3", "flac"))
    require(musicFiles.nonEmpty, s"Could not find any songs in $dir - maybe they're in subfolders...")

    val hasNonTrivialDiscNumber = false == musicFiles // as opposed to 1/1 - Fuck those guys.
        .map(AudioFileIO.read)
        .hasSameValues(_.getTag getFirst FieldKey.DISC_NO)

    val (year, album) = musicFiles.head.mapTo(Song.apply)
        .mapTo(firstSong => retrieveYear(firstSong) -> StringFixer(firstSong.albumName))

    musicFiles foreach (fixFile(_, hasNonTrivialDiscNumber))
    musicFiles foreach rename

    try {
      val renamedFolder = new File(dir.parent, s"$year ${album filterNot reservedDirCharacters}")
      val result = dir.dir renameTo renamedFolder
      assert(result, s"Failed to rename directory to <$renamedFolder>")
      Directory(renamedFolder)
    } catch {
      case e: Exception => throw new Exception("could not rename the folder", e)
    }
  }
}
