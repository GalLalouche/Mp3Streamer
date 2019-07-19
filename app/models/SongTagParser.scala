package models

import java.io.File
import java.util.logging.{Level, Logger}

import com.google.common.annotations.VisibleForTesting
import models.RichTag._
import org.jaudiotagger.audio.{AudioFile, AudioFileIO}
import org.jaudiotagger.tag.{FieldKey, Tag}

import scala.util.Try

import common.io.IOFile
import common.rich.path.RichFile._
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichOption._
import common.rich.RichT._

object SongTagParser {
  Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF)

  private val yearPattern = """(\d{4})""".r.unanchored

  private case class OptionalFields(
      trackGain: Option[Double],
      performanceYear: Option[Int],
  )
  private def parseReplayGain(s: String): Double = s.split(' ').head.toDouble
  // FLAC tag supports proper custom tag fetching, but MP3 tags have to be parsed manually
  private def parseFlacTag(tag: Tag) = OptionalFields(
    trackGain = tag.firstNonEmpty("REPLAYGAIN_TRACK_GAIN").map(parseReplayGain),
    performanceYear = tag.firstNonEmpty("PERFORMANCEYEAR").map(_.toInt),
  )
  private def parseMp3Tag(tag: Tag) = {
    val ct = tag.customTags
    OptionalFields(
      trackGain = ct.get("replaygain_track_gain").map(parseReplayGain),
      performanceYear = ct.get("PERFORMANCEYEAR").map(_.toInt),
    )
  }

  def apply(file: File): IOSong = {
    require(file.exists, file + " doesn't exist")
    require(file.isDirectory.isFalse, file + " is a directory")
    apply(file, AudioFileIO read file)
  }
  @VisibleForTesting private[models] def extractYearFromName(s: String): Option[Int] = {
    val v = yearPattern.findAllIn(s).toVector
    require(v.size <= 1)
    v.headOption.map(_.toInt)
  }
  def apply(file: File, audioFile: AudioFile): IOSong = {
    val (tag, header) = audioFile.toTuple(_.getTag, _.getAudioHeader)
    val year =
      Try(yearPattern.findAllIn(tag.getFirst(FieldKey.YEAR)).matchData.next().group(1).toInt)
          .toOption.orElse(extractYearFromName(file.parent.name))
          .getOrThrow(s"No year in <$file>")
    val optionalFields = if (file.extension.toLowerCase == "flac") parseFlacTag(tag) else parseMp3Tag(tag)

    IOSong(
      file = IOFile(file),
      title = tag.getFirst(FieldKey.TITLE),
      artistName = tag.getFirst(FieldKey.ARTIST),
      albumName = tag.getFirst(FieldKey.ALBUM),
      track = tag.getFirst(FieldKey.TRACK).takeWhile(_.isDigit).toInt, // takeWhile handles 01/08 formats.
      year = year,
      bitRate = header.getBitRate,
      duration = header.getTrackLength,
      size = file.length,
      discNumber = tag.firstNonEmpty(FieldKey.DISC_NO),
      trackGain = optionalFields.trackGain,
      composer = tag.firstNonEmpty(FieldKey.COMPOSER),
      conductor = tag.firstNonEmpty(FieldKey.CONDUCTOR),
      orchestra = tag.firstNonEmpty(FieldKey.ORCHESTRA),
      opus = tag.firstNonEmpty(FieldKey.OPUS).map(_.mapIf(_.head.isDigit).to("Op. " + _)),
      performanceYear = optionalFields.performanceYear,
    )
  }
}
