package backend.recon

import models.Song

sealed trait Reconcilable
case class Artist(name: String) extends Reconcilable
case class Album(name: String, artist: Artist) extends Reconcilable {
  def artistName: String = artist.name
}
case class Track(title: String, album: Album) extends Reconcilable {
  def artistName = album.artist.name
  def albumName = album.name
}

object Reconcilable {
  implicit class SongExtractor(s: Song) {
    lazy val artist = Artist(s.artistName)
    lazy val album = Album(s.albumName, artist)
    lazy val track = Track(s.title, album)
  }
}
