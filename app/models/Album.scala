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
import common.path.Path._
import common.path.RichFile
import common.path.Directory
import common.Jsoner
import play.api.libs.json.JsArray
/**
  * Handles parsing mp3 data
  */
class Album(val dir: Directory) {
	require(dir != null)
	
	val songs = dir.files.filter(x => List("mp3", "flac").contains(x.extension)).map(Song(_)).sortBy(_.track)
	
	def jsonify = {
		JsArray(songs.map(_.jsonify))
	}
}

object Album {
	def apply(f: File) = new Album(Directory(f))
}