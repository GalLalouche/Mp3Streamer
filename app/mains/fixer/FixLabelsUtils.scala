package mains.fixer

import java.io.File
import java.util.logging.{Level, Logger}
import java.util.regex.Pattern

import com.google.common.annotations.VisibleForTesting
import models.{Song, SongTagParser}
import org.jaudiotagger.audio.{AudioFile, AudioFileIO}
import org.jaudiotagger.tag.{FieldKey, Tag}
import org.jaudiotagger.tag.flac.FlacTag
import org.jaudiotagger.tag.id3.ID3v24Tag

import scala.annotation.tailrec

import common.rich.RichT.{richT, _}
import common.rich.path.RichFile.richFile
import common.rich.primitives.RichInt._
import common.rich.primitives.RichString.richString

private[mains] object FixLabelsUtils {
  Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF)

  private val NumberFollowedBySlash = Pattern compile """\d+[/\\].*"""
  private val InvalidFileCharacters = Pattern compile """[:\\/*?|<>]"""
  private val MultiSpace = Pattern compile " +"

  private def properTrackString(track: Int): String = track padLeftZeros 2
  @VisibleForTesting
  def getFixedTag(f: File, fixDiscNumber: Boolean): Tag = getFixedTag(f, fixDiscNumber, AudioFileIO read f)

  private val BonusTrackSuffixes = Vector("bonus", "bonus track").map("(" + _ + ")")
  // If fixDiscNumber is false, it will be removed, unless the title indicates it is a bonus track.
  def getFixedTag(f: File, fixDiscNumber: Boolean, audioFile: AudioFile): Tag = {
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
    // Not all track need to have a disc number property, e.g., bonus track.
    song.discNumber
        .filter(fixDiscNumber.const)
        // Replace 1/2 with 1
        .map(_.mapIf(_.matches(NumberFollowedBySlash)).to(_.takeWhile(_.isDigit)))
        .foreach($.setField(FieldKey.DISC_NO, _))

    val lowerCasedTitle = $.getFirst(FieldKey.TITLE).toLowerCase
    if (BonusTrackSuffixes.exists(lowerCasedTitle.endsWith)) {
      val titleWithoutLastParens = lowerCasedTitle.dropAfterLast('(').dropRight(2)
      set(FieldKey.TITLE, titleWithoutLastParens)
      $.setField(FieldKey.DISC_NO, "Bonus")
    }

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
  def validFileName(requestedFileName: String): String =
    requestedFileName.removeAll(InvalidFileCharacters).replaceAll(MultiSpace, " ")

  def newFileName(song: Song, extension: String): String =
    s"${properTrackString(song.track)} - ${validFileName(song.title)}.$extension"
}
