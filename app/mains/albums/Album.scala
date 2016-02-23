package models

import java.io.File
import common.rich.path.Directory

case class Album(artist: String, year: Int, albumName: String)

object Album {
	def apply(s: Song) = new Album(s.artist, s.year, s.album)
	def apply(d: Directory) = {
		val split = d.name.split("\\s+")
		val year = split(0).toInt
		val albumName = split(1)
		val artist = d.parent.name
		new Album(artist, year, albumName)	
	}
}