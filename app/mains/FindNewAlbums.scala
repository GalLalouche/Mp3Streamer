package mains

import java.io.File
import common.rich.path.RichFile.richFile
import controllers.MusicLocations
import models.MusicFinder
import other.{ MusicBrainzRetriever, NewAlbumsRetriever }
import common.rich.path.Directory

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
		val f = Directory("C:/").addFile("albums.txt")
		println($.findNewAlbums.foreach(a => {
			println(a)
			f appendLine a.toString
		}))
	}
}