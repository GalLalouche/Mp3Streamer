package models

import java.io.File
import java.util.logging.{Level, Logger}

import common.io.IOFile
import common.rich.path.RichFile._
import common.rich.RichT._
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichString._
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.{FieldKey, Tag}

import scala.collection.JavaConverters._

private object SongTagParser {
  Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF)

  private val customPattern =
    """Description="([^"]+)"; Text="([^"]+)"; """.r
  private val yearPattern = """.*(\d{4}).*""".r

  private implicit class RichTag(private val $: Tag) extends AnyVal {
    private def filterNonEmpty(s: String): Option[String] = s.trim.opt.filter(_.nonEmpty)
    def firstNonEmpty(key: String): Option[String] = $.getFirst(key) |> filterNonEmpty
    def firstNonEmpty(key: FieldKey): Option[String] = $.getFirst(key) |> filterNonEmpty
  }

  private case class OptionalFields(trackGain: Option[Double], opus: Option[Int], performanceYear: Option[Int])
  private def parseReplayGain(s: String): Double = s.split(' ').head.toDouble
  // FLAC tag supports proper custom tag fetching, but MP3 tags have to be parsed manually
  private def parseFlacTag(tag: Tag) = OptionalFields(
    trackGain = tag.firstNonEmpty("REPLAYGAIN_TRACK_GAIN").map(parseReplayGain),
    opus = tag.firstNonEmpty("OPUS").map(_.toInt),
    performanceYear = tag.firstNonEmpty("PERFORMANCEYEAR").map(_.toInt),
  )
  private def parseMp3Tag(tag: Tag) = {
    val customTags = tag.getFields("TXXX").asScala
        .map(_.toString)
        .filter(_ matches customPattern.pattern)
        .map({case customPattern(key, value) => key -> value})
        .toMap
    OptionalFields(
      trackGain = customTags.get("replaygain_track_gain").map(parseReplayGain),
      opus = customTags.get("OPUS").map(_.toInt),
      performanceYear = customTags.get("PERFORMANCEYEAR").map(_.toInt),
    )
  }

  def apply(file: File): IOSong = {
    require(file != null)
    require(file.exists, file + " doesn't exist")
    require(file.isDirectory.isFalse, file + " is a directory")
    val (tag, header) = AudioFileIO.read(file).mapTo(e => (e.getTag, e.getAudioHeader))
    val year = try
      yearPattern.findAllIn(tag.getFirst(FieldKey.YEAR)).matchData.next().group(1).toInt
    catch {
      case _: MatchError =>
        println(s"No year in $file")
        0 // Some songs, e.g., classical, don't have a year yet.
    }
    val optionalFields = if (file.extension.toLowerCase == "flac") parseFlacTag(tag) else parseMp3Tag(tag)

    IOSong(
      file = IOFile(file),
      title = tag.getFirst(FieldKey.TITLE),
      artistName = tag.getFirst(FieldKey.ARTIST),
      albumName = tag.getFirst(FieldKey.ALBUM),
      track = tag.getFirst(FieldKey.TRACK).toInt,
      year = year,
      bitRate = header.getBitRate,
      duration = header.getTrackLength,
      size = file.length,
      discNumber = tag.firstNonEmpty(FieldKey.DISC_NO),
      trackGain = optionalFields.trackGain,
      composer = tag.firstNonEmpty(FieldKey.COMPOSER),
      opus = optionalFields.opus,
      performanceYear = optionalFields.performanceYear,
    )
  }
}
