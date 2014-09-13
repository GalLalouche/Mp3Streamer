package models

case class Album(artist: String, year: Int, albumName: String)

object Album {
	def apply(s: Song) = new Album(s.artist, s.year, s.album)
}