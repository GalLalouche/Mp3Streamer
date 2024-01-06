package backend.recon

import models.Album.AlbumTitle
import models.Song
import models.Song.SongTitle

import monocle.macros.Lenses

sealed trait Reconcilable {
  def normalize: String
}
@Lenses
case class Artist(name: String) extends Reconcilable {
  override def normalize: String = name.toLowerCase
  def normalized: Artist = Artist(normalize)
}
case class Album(title: AlbumTitle, year: Int, artist: Artist) extends Reconcilable {
  def artistName: String = artist.name
  override def normalize: String = s"${artist.normalize} - ${title.toLowerCase}"
  def normalized: Album = copy(title = title.toLowerCase, artist = artist.normalized)
}
case class Track(title: SongTitle, album: Album) extends Reconcilable {
  def artistName: String = album.artist.name
  def albumName: AlbumTitle = album.title
  override def normalize: String = ???
}

object Reconcilable {
  implicit class SongExtractor(s: Song) {
    lazy val artist = Artist(s.artistName)
    lazy val release = Album(s.albumName, s.year, artist)
    lazy val track = Track(s.title, release)
  }
}
