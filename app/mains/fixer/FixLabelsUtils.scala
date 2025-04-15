package mains.fixer

import java.util.regex.Pattern

import com.google.common.annotations.VisibleForTesting
import com.google.inject.Inject
import mains.fixer.FixLabelsUtils.{BonusTrackSuffixes, InvalidFileCharacters, MultiSpace, NumberFollowedBySlash}
import models.{IOSongTagParser, Song, TrackNumber}
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.tag.{FieldKey, Tag}
import org.jaudiotagger.tag.flac.FlacTag
import org.jaudiotagger.tag.id3.ID3v24Tag

import scala.annotation.tailrec

import common.rich.RichT._
import common.rich.path.RichFile.richFile
import common.rich.primitives.RichInt.Rich
import common.rich.primitives.RichString.richString

private[mains] class FixLabelsUtils @Inject() (stringFixer: StringFixer) {
  // If fixDiscNumber is false, it will be removed, unless the title indicates it is a bonus track.
  def getFixedTag(audioFile: AudioFile, fixDiscNumber: Boolean): Tag = {
    val song = IOSongTagParser(audioFile)
    val $ = if (audioFile.getFile.extension.equalsIgnoreCase("flac")) new FlacTag else new ID3v24Tag

    @tailrec
    def set(key: FieldKey, a: Any): Unit = a match {
      case s: String => $.setField(key, stringFixer(s))
      case i: Int => $.setField(key, i.toString)
      case None => ()
      case Some(x) => set(key, x)
    }
    set(FieldKey.ARTIST, song.artistName)
    set(FieldKey.TITLE, song.title)
    set(FieldKey.ALBUM, song.albumName)
    set(FieldKey.YEAR, song.year)
    $.setField(FieldKey.TRACK, properTrackString(song.trackNumber))
    // Not all track need to have a disc number property, e.g., bonus track.
    song.discNumber
      .filter(fixDiscNumber.const)
      // Replace 1/2 with 1
      .map(_.mapIf(_.matches(NumberFollowedBySlash)).to(_.takeWhile(_.isDigit)))
      .foreach($.setField(FieldKey.DISC_NO, _))

    val lowerCasedTitle = $.getFirst(FieldKey.TITLE).toLowerCase
    if (isBonusTrack(lowerCasedTitle)) {
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

  @VisibleForTesting
  private[fixer] def isBonusTrack(s: String): Boolean = BonusTrackSuffixes.exists(s.endsWith)

  def validFileName(requestedFileName: String): String =
    requestedFileName.removeAll(InvalidFileCharacters).replaceAll(MultiSpace, " ")

  def newFileName(song: Song, extension: String): String =
    s"${properTrackString(song.trackNumber)} - ${validFileName(song.title)}.$extension"

  private def properTrackString(track: TrackNumber): String = track.padLeftZeros(2)
}

private object FixLabelsUtils {
  private val NumberFollowedBySlash = Pattern.compile("""\d+[/\\].*""")
  private val InvalidFileCharacters = Pattern.compile("""[:\\/*?|<>"]""")
  private val MultiSpace = Pattern.compile(" +")

  private val BonusTrackSuffixes = for {
    str <- Vector("bonus", "bonus track")
    (s, e) <- Vector(('(', ')'), ('[', ']'), ('<', '>))
  } yield s"$s$str$e"
}
