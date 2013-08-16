package models

import java.io.File
import scala.annotation.implicitNotFound
import scala.reflect.runtime.{ universe => ru }
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import play.api.libs.json.JsString
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.libs.json.JsNumber
import play.api.libs.json.JsNumber
import play.api.libs.json.JsArray
import play.api.libs.json.JsBoolean
import play.api.libs.json.JsValue
import common.Path._
import common.RichFile
/**
  * Handles parsing mp3 data
  */
class Song(val file: RichFile) {
	require(file != null)
	require(file exists)
	require(file.isDirectory == false)

	private val (tag, header) = {
		val x = (AudioFileIO.read(file))
		(x.getTag, x.getAudioHeader)
	}
	// get ID3 info
	val title = tag.getFirst(FieldKey.TITLE)
	val artist = tag.getFirst(FieldKey.ARTIST)
	val album = tag.getFirst(FieldKey.ALBUM)
	val track = tag.getFirst(FieldKey.TRACK).toInt
	val year = {
		val regexp = ".*(\\d{4}).*".r
		val regexp(result) = tag.getFirst(FieldKey.YEAR)
		result.toInt
	}
	val bitrate = header.getBitRate()
	val duration = header.getTrackLength()
	val size = file.length

	override def toString = "%s - %s [%s #%d] (%s)".format(artist, title, album, track, year)

	def jsonify = Json obj (
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
	def apply(f: File) = new Song(new RichFile(f))
}