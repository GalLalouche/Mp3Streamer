package models

import java.io.File
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import common.path.Path.poorPath
import common.path.RichFile
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import scala.MatchError
/**
  * Handles parsing mp3 data
  */
class Song(val file: File) {
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