//package mains.albums
//
//import common.rich.path.Directory
//import models.Song
//
//private case class Album(artist: String, year: Int, albumName: String)
//
//private object Album {
//	def apply(s: Song) = new Album(s.artistName, s.year, s.albumName)
//	def apply(d: Directory) = {
//		val split = d.name.split("\\s+")
//		val year = split(0).toInt
//		val albumName = split(1)
//		val artist = d.parent.name
//		new Album(artist, year, albumName)
//	}
//}
