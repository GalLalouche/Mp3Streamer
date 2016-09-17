package models

import java.io.File
import java.util.logging.{Level, Logger}

import common.rich.RichT._
import common.rich.primitives.RichString._
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

import scala.collection.JavaConversions._

case class Song(file: File, title: String, artistName: String, albumName: String,
                track: Int, year: Int, bitrate: String, duration: Int, size: Long,
                discNumber: Option[String], trackGain: Option[Double]) {
  override def toString = s"$artistName - $title [$albumName #$track] ($year)"
  lazy val album = Album(dir = file.getParentFile, title = albumName, artistName = artistName, year = year)
}

object Song {
  Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF)

  /** Parses ID3 data */
  def apply(file: File): Song = {
    require(file != null)
    require(file.exists, file + " doesn't exist")
    require(file.isDirectory == false, file + " is a directory")
    val (tag, header) = {
      val x = AudioFileIO.read(file)
      (x.getTag, x.getAudioHeader) // issues with running this using java if it uses ->
    }
    // get ID3 info
    val title = tag.getFirst(FieldKey.TITLE)
    val artist = tag.getFirst(FieldKey.ARTIST)
    val album = tag.getFirst(FieldKey.ALBUM)
    val track = tag.getFirst(FieldKey.TRACK).toInt
    val year = try {
      ".*(\\d{4}).*".r.findAllIn(tag.getFirst(FieldKey.YEAR)).matchData.next().group(1).toInt
    } catch {
      case _: MatchError =>
        println(s"No year in $file")
        0
    }
    val bitrate = header.getBitRate
    val duration = header.getTrackLength
    val size = file.length
    // in flac files, DISC_NO works. In regular files, it doesn't so it needs to be parsed manually :\
    def parseDiscNumber(s: String): String =
    s.dropWhile(_ != '=').drop(2).reverse.dropWhile(_ != ';').drop(2).reverse
    val discNumber = tag.getFields("TPOS").toString.opt.filter(_.length >= 3).map(parseDiscNumber)
        .orElse(tag.getFirst(FieldKey.DISC_NO).opt.filter(_.nonEmpty))
    def parseReplayGain(s: String): String = s.dropAfterLast('=').drop(1).takeWhile(_ != '"')
    // in flac files, REPLAYGAIN_TRACK_GAIN works. In regular files, it doesn't so it needs to be parsed manually :\
    val trackGain = tag.getFields("REPLAYGAIN_TRACK_GAIN").headOption.map(_.toString)
        .orElse(tag.getFields("TXXX").map(_.toString).find(_.contains("track_gain")) map parseReplayGain)
        .map(_.split(' ').apply(0).toDouble)

    new Song(file = file, title = title, artistName = artist, albumName = album, track = track,
      year = year, bitrate = bitrate, duration = duration, size = size, discNumber = discNumber, trackGain = trackGain)
  }
}
