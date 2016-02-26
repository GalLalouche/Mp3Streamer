package models

import java.io.File
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import common.rich.path.RichPath._
import common.rich.path.RichFile
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import scala.MatchError
import java.util.logging.Logger
import java.util.logging.Level
import play.api.libs.json.JsObject
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue

/**
  * Handles parsing mp3 data
  */
class Song(val file: File, val title: String, val artistName: String, val albumName: String,
    val track: Int, val year: Int, val bitrate: String, val duration: Int, val size: Long) {
  override def toString = "%s - %s [%s #%d] (%s)".format(artistName, title, albumName, track, year)
  lazy val album = Album(file.parent)
}

object Song {
  Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF) // STFU already!
  def apply(file: File): Song = {
    require(file != null)
    require(file exists)
    require(file.isDirectory == false)
    val (tag, header) = {
      val x = (AudioFileIO.read(file))
      (x.getTag, x.getAudioHeader)
    }
    // get ID3 info
    val title = tag.getFirst(FieldKey.TITLE)
    val artist = tag.getFirst(FieldKey.ARTIST)
    val album = tag.getFirst(FieldKey.ALBUM)
    val track = tag.getFirst(FieldKey.TRACK).toInt
    val year = {
      try {

        val regexp = ".*(\\d{4}).*".r
        val regexp(result) = tag.getFirst(FieldKey.YEAR)
        result.toInt
      } catch {
        case _: MatchError => println(s"No year in $file"); 0
      }
    }
    val bitrate = header.getBitRate()
    val duration = header.getTrackLength()
    val size = file.length

    new Song(file = file, title = title, artistName = artist, albumName = album, track = track,
      year = year, bitrate = bitrate, duration = duration, size = size)
  }
}
