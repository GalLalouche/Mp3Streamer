//package mains.albums
//
//import java.io.File
//
//import common.rich.RichT._
//import common.rich.path.Directory
//import common.rich.path.RichFile.richFile
//import controllers.RealLocations
//
//object FindNewAlbums {
//	def main(args: Array[String]) {
//		val ignoredBands = new File(getClass.getResource("ignoredBands").getFile).lines
//		val $ = new NewAlbumsRetriever( MusicBrainzRetriever, ignoredBands)
//		val file = Directory("C:/").addFile("albums.txt").clear()
//		$.findNewAlbums.map(_.toString.log()).foreach(file appendLine)
//		println("Done")
//	}
//}
