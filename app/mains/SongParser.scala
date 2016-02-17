package mains

import common.Debug
import controllers.MusicLocations
import models._
import play.api.libs.json.{ JsObject, Json }
import java.io.File

object SongParser extends Debug {
	def main(args: Array[String]) {
		timed("parsing all files") {
			val mf = new MusicFinder with MusicLocations
			val file = mf.genreDirs.head.parent.addFile("songs.json").clear()
			val songs = mf.getSongFilePaths.map(new File(_)).map(Song.apply).map(_.jsonify.toString()).foreach(file.appendLine)
			//			val songs = file.lines.map(Json.parse).map(_.as[JsObject]).map(Song.apply)
			//			val s = new SimpleMusicSearcher(songs)
			//			println(s.apply("Angela Hewitt"))
		}
	}
}
