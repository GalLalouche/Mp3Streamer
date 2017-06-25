package models

import java.io.File
import java.util.logging.{Level, Logger}

import common.io.{FileRef, IOFile}
import common.rich.RichT._
import common.rich.primitives.RichString._
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

import scala.collection.JavaConversions._

case class Song(file: FileRef, iofile: File, title: String, artistName: String, albumName: String,
                track: Int, year: Int, bitRate: String, duration: Int, size: Long,
                discNumber: Option[String], trackGain: Option[Double]) {
  lazy val album = Album(dir = iofile.getParentFile, title = albumName, artistName = artistName, year = year)
}

object Song {
  Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF)

  /** Parses ID3 data */
  def apply(file: File): Song = {
    require(file != null)
    require(file.exists, file + " doesn't exist")
    require(file.isDirectory == false, file + " is a directory")
    val (tag, header) = AudioFileIO.read(file).mapTo(e => (e.getTag, e.getAudioHeader))
    val year = try {
      ".*(\\d{4}).*".r.findAllIn(tag.getFirst(FieldKey.YEAR)).matchData.next().group(1).toInt
    } catch {
      case _: MatchError =>
        println(s"No year in $file")
        0 // Some songs, e.g., classical, don't have a year yet.
    }
    val discNumber = Option(tag.getFirst(FieldKey.DISC_NO)).filterNot(_.isEmpty)
    def parseReplayGain(s: String): String = s.dropAfterLast('=').drop(1).takeWhile(_ != '"')
    // in flac files, REPLAYGAIN_TRACK_GAIN works. In regular files, it doesn't so it needs to be parsed manually :\
    val trackGain = (tag.getFields("REPLAYGAIN_TRACK_GAIN").headOption map (_.toString))
        .orElse(tag getFields "TXXX" map (_.toString) find (_ contains "track_gain") map parseReplayGain)
        .map(_.split(' ').apply(0).toDouble) // handle the case of "1.43 dB"

    new Song(file = IOFile(file), iofile = file, title = tag.getFirst(FieldKey.TITLE),
      artistName = tag.getFirst(FieldKey.ARTIST), albumName = tag.getFirst(FieldKey.ALBUM),
      track = tag.getFirst(FieldKey.TRACK).toInt, year = year, bitRate = header.getBitRate,
      duration = header.getTrackLength, size = file.length, discNumber = discNumber, trackGain = trackGain)
  }
}
