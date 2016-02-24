package mains

import common.Debug
import controllers.MusicLocations
import models._
import play.api.libs.json.{ JsObject, Json }
import java.io.File
import search.MetadataCacher

object SongParser extends Debug {
	def main(args: Array[String]) {
		timed("parsing all files") {
			MetadataCacher.apply(new MusicFinder with MusicLocations)
		}
	}
}
