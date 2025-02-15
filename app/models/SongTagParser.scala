package models

import java.io.File
import java.util.concurrent.TimeUnit

import com.google.common.annotations.VisibleForTesting
import org.jaudiotagger.audio.{AudioFile, AudioFileIO}
import org.jaudiotagger.tag.{FieldKey, Tag}

import scala.concurrent.duration.Duration
import scala.util.Try

import common.TagUtils.richTag
import common.io.IOFile
import common.rich.RichT._
import common.rich.collections.RichIterator._
import common.rich.collections.RichTraversableOnce._
import common.rich.path.RichFile._
import common.rich.primitives.RichOption._

object SongTagParser {
  def apply(file: File): IOSong = apply(file, AudioFileIO.read(file))
  def apply(file: File, audioFile: AudioFile): IOSong = {
    val (tag, header) = audioFile.toTuple(_.getTag, _.getAudioHeader)
    val year = extractYear(file, tag).getOrThrow(s"No year in <$file>")
    IOSong(
      file = IOFile(file),
      title = tag.getFirst(FieldKey.TITLE),
      artistName = tag.getFirst(FieldKey.ARTIST),
      albumName = tag.getFirst(FieldKey.ALBUM),
      trackNumber = parseTrack(tag.getFirst(FieldKey.TRACK)),
      year = year,
      bitRate = header.getBitRate,
      duration = Duration(header.getTrackLength, TimeUnit.SECONDS),
      size = file.length,
      discNumber = tag.firstNonEmpty(FieldKey.DISC_NO),
      trackGain =
        tag.firstNonEmpty(FieldKey.REPLAYGAIN_TRACK_GAIN).flatMap(parseReplayGain(_).toOption),
      composer = tag.firstNonEmpty(FieldKey.COMPOSER),
      conductor = tag.firstNonEmpty(FieldKey.CONDUCTOR),
      orchestra = tag.firstNonEmpty(FieldKey.ORCHESTRA),
      opus = tag.firstNonEmpty(FieldKey.OPUS).map(_.mapIf(_.head.isDigit).to("Op. " + _)),
      performanceYear = tag.firstNonEmpty(FieldKey.PERFORMANCE_YEAR).map(_.toInt),
    )
  }

  // FLAC tag supports proper custom tag fetching, but MP3 tags have to be parsed manually
  private def parseReplayGain(s: String): Try[Double] = Try(s.split(' ').head.toDouble)

  def parseTrack(s: String): TrackNumber =
    s.takeWhile(_.isDigit).toInt // takeWhile handles "01/08" formats.

  private val YearPattern = """(\d{4})""".r.unanchored
  private def findYear(s: String) = YearPattern.findAllIn(s)
  def extractYear(file: File, tag: Tag): Option[Int] =
    findYear(tag.getFirst(FieldKey.YEAR)).matchData
      .headOption()
      .map(_.group(1).toInt)
      .orElse(extractYearFromName(file.parent.name))
  @VisibleForTesting private[models] def extractYearFromName(s: String): Option[Int] =
    findYear(s).toSet.optFilter(_.size == 1).map(_.single.toInt)
}
