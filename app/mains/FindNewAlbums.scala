package mains

import other.NewAlbumsRetriever
import controllers.MusicLocations
import models.MusicFinder
import other.LastfmMetadataRetriever
import java.util.logging._

object FindNewAlbums {
	def main(args: Array[String]) = {
		Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF)
		val $ = new NewAlbumsRetriever {
			override val meta: LastfmMetadataRetriever = new LastfmMetadataRetriever
			override val music: MusicFinder = new MusicFinder with MusicLocations {
				override val subDirs = List("Rock", "Metal")
			}
		}
		println($.findNewAlbums.take(50).foreach(println))
	}
}