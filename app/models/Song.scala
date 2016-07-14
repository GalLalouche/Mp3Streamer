package models

import java.io.File
import java.util.logging.{Level, Logger}

import common.rich.path.RichPath._
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

import scala.util.matching.Regex.MatchIterator

case class Song(val file: File, val title: String, val artistName: String, val albumName: String,
           val track: Int, val year: Int, val bitrate: String, val duration: Int, val size: Long) {
  override def toString = s"$artistName - $title [$albumName #$track] ($year)"
  lazy val album = Album(dir = file.getParentFile, title = albumName, artistName = artistName, year = year)
}

object Song {
  Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF) // STFU already!
  /** Parses ID3 data */
  def apply(file: File): Song = {
    require(file != null)
    require(file.exists, file + " doesn't exist")
    require(file.isDirectory == false, file + " is a directory")
    val (tag, header) = {
      val x = AudioFileIO.read(file)
      (x.getTag, x.getAudioHeader)
    }
    // get ID3 info
    val title = tag.getFirst(FieldKey.TITLE)
    val artist = tag.getFirst(FieldKey.ARTIST)
    val album = tag.getFirst(FieldKey.ALBUM)
    val track = tag.getFirst(FieldKey.TRACK).toInt
    val year = {
      try {
        ".*(\\d{4}).*".r.findAllIn(tag.getFirst(FieldKey.YEAR)).matchData.next().group(1).toInt
      } catch {
        case _: MatchError =>
          println(s"No year in $file")
          0
      }
    }
    val bitrate = header.getBitRate
    val duration = header.getTrackLength
    val size = file.length

    new Song(file = file, title = title, artistName = artist, albumName = album, track = track,
      year = year, bitrate = bitrate, duration = duration, size = size)
  }
}
