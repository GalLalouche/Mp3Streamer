package mains

import other.NewAlbumsRetriever
import controllers.MusicLocations
import models.MusicFinder
import other.LastfmMetadataRetriever
import java.util.logging._
import other.MusicBrainzRetriever
import java.io.File
import common.path.RichFile._
object FindNewAlbums {
	def main(args: Array[String]) = {
		val $ = new NewAlbumsRetriever(MusicBrainzRetriever,
			new MusicFinder with MusicLocations { override val subDirs = List("Rock", "Metal") },
			Seq())
		val f = new File("C:/ProcessList.txt")
		println($.findNewAlbums.foreach(a => {
			println(a)
			f appendLine a.toString
		}))
	}
}