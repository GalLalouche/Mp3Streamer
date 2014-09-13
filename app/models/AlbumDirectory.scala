package models

import java.io.File

import common.path.Directory
import common.path.RichFile._
import play.api.libs.json.JsArray
/**
  * Handles parsing mp3 data
  */
class AlbumDirectory(val dir: Directory) {
	require(dir != null)
	
	val songs = dir.files.filter(x => List("mp3", "flac").contains(x.extension)).map(Song(_)).sortBy(_.track)
	
	def jsonify = {
		JsArray(songs.map(_.jsonify))
	}
}

object AlbumDirectory {
	def apply(f: File) = new AlbumDirectory(Directory(f))
}