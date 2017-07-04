package backend.recon

import models.Song
import monocle.macros.Lenses

sealed trait Reconcilable {
  def normalize: String
}
@Lenses
case class Artist(name: String) extends Reconcilable {
  override def normalize: String = name.toLowerCase
}
case class Album(title: String, year: Int, artist: Artist) extends Reconcilable {
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
    lazy val release = Album(s.albumName, s.year, artist)
    lazy val track = Track(s.title, release)
  }
}
