package models

import java.io.File
import java.util.logging.{Level, Logger}

import common.io.{FileRef, IOFile, MemoryFile}
import common.rich.RichT._
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichString._
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

import scala.collection.JavaConverters._

trait Song {
  type F <: FileRef
  def file: F
  def title: String
  def artistName: String
  def albumName: String
  def track: Int
  def year: Int
  def bitRate: String
  def duration: Int
  def size: Long
  def discNumber: Option[String]
  def trackGain: Option[Double]
  def album(implicit mfp: MusicFinderProvider) =
    Album(dir = file.parent, title = albumName, artistName = artistName, year = year, songs = mfp.mf getSongsInDir file.parent)
}

// TODO remove code duplication? hmm...
case class IOSong(file: IOFile, title: String, artistName: String, albumName: String,
                  track: Int, year: Int, bitRate: String, duration: Int, size: Long,
                  discNumber: Option[String], trackGain: Option[Double]) extends Song {
  override type F = IOFile
}

case class MemorySong(file: MemoryFile, title: String, artistName: String, albumName: String,
                      track: Int, year: Int, bitRate: String, duration: Int, size: Long,
                      discNumber: Option[String], trackGain: Option[Double]) extends Song {
  override type F = MemoryFile
}

object Song {
  Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF)

  /** Parses ID3 data */
  def apply(file: File): IOSong = {
    require(file != null)
    require(file.exists, file + " doesn't exist")
    require(file.isDirectory.isFalse, file + " is a directory")
    val (tag, header) = AudioFileIO.read(file).mapTo(e => (e.getTag, e.getAudioHeader))
    val year = try {
      ".*(\\d{4}).*".r.findAllIn(tag.getFirst(FieldKey.YEAR)).matchData.next().group(1).toInt
    } catch {
      case _: MatchError =>
        println(s"No year in $file")
        0 // Some songs, e.g., classical, don't have a year yet.
    }
    val discNumber = Option(tag.getFirst(FieldKey.DISC_NO)).map(_.trim).filterNot(_.isEmpty)
    def parseReplayGain(s: String): String = s.dropAfterLast('=').drop(1).takeWhile(_ != '"')
    // in flac files, REPLAYGAIN_TRACK_GAIN works. In regular files, it doesn't so it needs to be parsed manually :\
    val trackGain = (tag.getFields("REPLAYGAIN_TRACK_GAIN").asScala.headOption map (_.toString))
        .orElse(tag.getFields("TXXX").asScala map (_.toString) find (_ contains "track_gain") map parseReplayGain)
        .map(_.split(' ').apply(0).toDouble) // handle the case of "1.43 dB"

    IOSong(file = IOFile(file), title = tag.getFirst(FieldKey.TITLE),
      artistName = tag.getFirst(FieldKey.ARTIST), albumName = tag.getFirst(FieldKey.ALBUM),
      track = tag.getFirst(FieldKey.TRACK).toInt, year = year, bitRate = header.getBitRate,
      duration = header.getTrackLength, size = file.length, discNumber = discNumber, trackGain = trackGain)
  }
}
