package mains.albums

import java.io.File

import common.rich.path.Directory
import common.rich.path.RichFile.richFile
import controllers.MusicLocations
import models.MusicFinder

object FindNewAlbums {
	def main(args: Array[String]) {
		val ignoredBands = new File(getClass
			.getResource("ignoredBands")
			.getFile)
			.lines
			.toSeq
		val $ = new NewAlbumsRetriever(MusicBrainzRetriever,
			new MusicFinder with MusicLocations {
				override val subDirs = List("Rock", "Metal")
			}, ignoredBands)
		val f = Directory("C:/").addFile("albums.txt").clear()
		println($.findNewAlbums.foreach(a => {
			println(a)
			f appendLine a.toString
		}))
	}
}