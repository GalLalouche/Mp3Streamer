package mains.fixer

import java.io.File
import java.util.logging.{Level, Logger}
import java.util.regex.Pattern

import com.google.common.annotations.VisibleForTesting
import common.rich.RichT.{richT, _}
import common.rich.collections.RichTraversableOnce._
import common.rich.path.Directory
import common.rich.path.RichFile.richFile
import common.rich.path.RichPath.poorPath
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichString.richString
import models.{IOSong, SongTagParser}
import org.jaudiotagger.audio.{AudioFile, AudioFileIO}
import org.jaudiotagger.tag.{FieldKey, Tag}
import org.jaudiotagger.tag.flac.FlacTag
import org.jaudiotagger.tag.id3.ID3v24Tag

import scala.annotation.tailrec

/** Fixes ID3 tags on mp3 and flac files to proper casing, delete unused tags, etc. */
private object FixLabels {
  Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF)
  private def properTrackString(track: Int): String = if (track < 10) "0" + track else track.toString
  @VisibleForTesting
  private[fixer] def getFixedTag(f: File, fixDiscNumber: Boolean): Tag =
    getFixedTag(f, fixDiscNumber, AudioFileIO read f)
  private val NumberFollowedBySlash = Pattern compile """\d+[/\\].*"""
  private def getFixedTag(f: File, fixDiscNumber: Boolean, audioFile: AudioFile): Tag = {
    val song = SongTagParser(f, audioFile)
    val $ = if (f.extension.toLowerCase == "flac") new FlacTag else new ID3v24Tag

    @tailrec
    def set(key: FieldKey, a: Any): Unit = a match {
      case s: String => $.setField(key, StringFixer(s))
      case i: Int => $.setField(key, i.toString)
      case None => ()
      case Some(x) => set(key, x)
    }
    set(FieldKey.ARTIST, song.artistName)
    set(FieldKey.TITLE, song.title)
    set(FieldKey.ALBUM, song.albumName)
    set(FieldKey.YEAR, song.year)
    $.setField(FieldKey.TRACK, properTrackString(song.track))
    // Not all track need to have a disc number property, e.g., bonus track
    song.discNumber
        .filter(fixDiscNumber.const)
        // Replace 1/2 with 1
        .map(_.mapIf(_.matches(NumberFollowedBySlash)).to(_.takeWhile(_.isDigit)))
        .foreach($.setField(FieldKey.DISC_NO, _))
    // Performance year should only exist if it was manually added, i.e., we can assume the user added other
    // classical tags. Otherwise, we can assume they're BS and delete them (by not copying them from the
    // original tag).
    if (song.performanceYear.isDefined) {
      set(FieldKey.COMPOSER, song.composer)
      set(FieldKey.CONDUCTOR, song.conductor)
      set(FieldKey.ORCHESTRA, song.orchestra)
      set(FieldKey.OPUS, song.opus)
      set(FieldKey.PERFORMANCE_YEAR, song.performanceYear)
    }

    $
  }

  private def fixFile(f: File, fixDiscNumber: Boolean): Unit = {
    val audioFile = AudioFileIO read f
    val newTag = getFixedTag(f, fixDiscNumber, audioFile)
    AudioFileIO delete audioFile
    audioFile setTag newTag
    audioFile.commit()
  }

  // TODO handle code duplication with FolderFix.findArtistFolder
  @VisibleForTesting
  private[fixer] def validFileName(requestedFileName: String): String =
    requestedFileName.replaceAll("""[:\\\\/*?|<>]""", "").replaceAll(" +", " ")
  private def newFileName(f: File): String = {
    val song = IOSong.read(f)
    s"${properTrackString(song.track)} - ${validFileName(song.title)}.${f.extension}"
  }

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

    // TODO replace with MusicFinder
    val musicFiles = dir.files.filter(_.extension |> Set("mp3", "flac"))
    require(musicFiles.nonEmpty, s"Could not find any songs in $dir - maybe they're in subfolders...")

    // As opposed to 1/1–Fuck those guys.
    val hasNonTrivialDiscNumber =
      musicFiles.map(AudioFileIO.read).hasSameValues(_.getTag.getFirst(FieldKey.DISC_NO)).isFalse

    musicFiles.foreach(fixFile(_, hasNonTrivialDiscNumber))
    musicFiles.foreach(f => f renameTo new File(f.parent, newFileName(f)))

    val (year, album) = dir.files.iterator
        .filter(_.extension |> Set("mp3", "flac"))
        .next.|>(IOSong.read).toTuple(_.year, _.albumName)
    // Also, a directory name cannot end in ".".
    val expectedName = s"$year ${validFileName(album).replaceAll("""\.$""", "")}"
    try
      new FixedDirectory(renameFolder(dir, expectedName), expectedName)
    catch {
      case e: Exception => throw new Exception("could not rename the folder", e)
    }
  }

  // This should never happen now that validFileName is used!
  def verify(dir: Directory): Boolean =
    dir.files.filter(_.extension |> Set("mp3", "flac")) forall (f => f.name == newFileName(f))
}
