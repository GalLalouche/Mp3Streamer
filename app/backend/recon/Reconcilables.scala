package backend.recon

import models.Song

sealed trait Reconcilable {
  def normalize: String
}
case class Artist(name: String) extends Reconcilable {
  override def normalize: String = name.toLowerCase
}
case class Album(title: String, artist: Artist) extends Reconcilable {
  def artistName: String = artist.name
  override def normalize: String = s"${artist.normalize} - ${title.toLowerCase}"
}
case class Track(title: String, album: Album) extends Reconcilable {
  def artistName = album.artist.name
  def albumName = album.title
  override def normalize: String = ???
}

object Reconcilable {
  implicit class SongExtractor(s: Song) {
    lazy val artist = Artist(s.artistName)
    lazy val release = Album(s.albumName, artist)
    lazy val track = Track(s.title, release)
  }
}
