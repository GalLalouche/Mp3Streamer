package mains

import common.Debug
import controllers.MusicLocations
import models._
import play.api.libs.json.{ JsObject, Json }
import java.io.File
import search.MetadataCacher
import controllers.Searcher
import search.TermIndexBuilder

object SongParser extends Debug {
	def main(args: Array[String]) {
		timed("parsing all files") {
//		  val index = TermIndexBuilder.buildIndexFor(MetadataCacher.load[Song])
//		  println(index.find("death"))
			MetadataCacher.apply(new MusicFinder with MusicLocations)
//		  Searcher.aux("black")
		}
		exit()
	}
}
