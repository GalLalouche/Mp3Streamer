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
class Song private(val file: File, val title: String, val artist: String, val album: String, 
    val track: Int, val year: Int, val bitrate: String, val duration: Int, val size: Long) {
	override def toString = "%s - %s [%s #%d] (%s)".format(artist, title, album, track, year)

	def jsonify = Json obj (
		"file" -> file.path,
		"title" -> title,
		"artist" -> artist,
		"album" -> album,
		"track" -> track,
		"year" -> year,
		"bitrate" -> bitrate,
		"duration" -> duration,
		"size" -> size)
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

    new Song(file, title, artist, album, track, year, bitrate, duration, size)
	}
	def apply(json: JsObject): Song = {
		def asString(s: String): String = json.\(s).as[String]
		def asInt(s: String): Int = json.\(s).as[Int]
		def asLong(s: String): Long = json.\(s).as[Long]

		val file = new File(asString("file"))
		new Song(file=file, asString("title"), artist=asString("artist"), album=asString("album"),
      track=asInt("track"), year=asInt("year"), bitrate=asString("bitrate"), 
      duration=asInt("duration"), size=asInt("size"))
	}
}
