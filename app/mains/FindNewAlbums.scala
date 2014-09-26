package mains

import java.io.File

import common.path.RichFile.richFile
import controllers.MusicLocations
import models.MusicFinder
import other.{ MusicBrainzRetriever, NewAlbumsRetriever }

object FindNewAlbums {
	def main(args: Array[String]) = {
		val ignoredBands = new File(getClass()
			.getResource("ignoredBands")
			.getFile())
			.lines
			.toSeq
		val $ = new NewAlbumsRetriever(MusicBrainzRetriever,
			new MusicFinder with MusicLocations {
				override val subDirs = List("Rock", "Metal")
			}, ignoredBands)
		val f = new File("C:/ProcessList.txt")
		println($.findNewAlbums.foreach(a => {
			println(a)
			f appendLine a.toString
		}))
	}
}