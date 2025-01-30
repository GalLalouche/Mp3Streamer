package backend.recon

import models.{Song, SongTitle}
import models.TypeAliases.{AlbumTitle, ArtistName}

sealed trait Reconcilable {
  def normalize: String
}

// Note that this is only a case class for interoperability with Slick. its implementation of
// hashcode and equals are different from the default ones!
case class Artist(name: ArtistName) extends Reconcilable {
  override def normalize: String = name.toLowerCase

  override def equals(other: Any): Boolean = other match {
    case that: Artist => this.name.equalsIgnoreCase(that.name)
    case _ => false
  }
  override def hashCode(): Int = normalize.hashCode
}

class Album(val title: AlbumTitle, val year: Int, val artist: Artist) extends Reconcilable {
  private lazy val normalized = title.toLowerCase
  override def normalize: String = s"${artist.normalize} - ${title.toLowerCase}"

  private def canEqual(other: Any): Boolean = other.isInstanceOf[Album]
  override def equals(other: Any): Boolean = other match {
    case that: Album =>
      that.canEqual(this) &&
      normalized == that.normalized &&
      year == that.year &&
      artist == that.artist
    case _ => false
  }
  override def hashCode(): Int = {
    val state = Seq(normalized, year, artist)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
  override def toString = s"Album($title, $year, $artist)"
}
object Album {
  def apply(title: AlbumTitle, year: Int, artist: Artist): Album = new Album(title, year, artist)
}

class Track(val title: SongTitle, val album: Album) extends Reconcilable {
  private lazy val normalized = title.toLowerCase
  def artist: Artist = album.artist
  override def normalize: String = ???

  private def canEqual(other: Any): Boolean = other.isInstanceOf[Track]
  override def equals(other: Any): Boolean = other match {
    case that: Track =>
      that.canEqual(this) &&
      normalized == that.normalized &&
      album == that.album
    case _ => false
  }
  override def hashCode(): Int = {
    val state = Seq(normalized, album)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"Track($title, $album)"
}
object Track {
  def apply(title: SongTitle, album: Album): Track = new Track(title, album)
}

object Reconcilable {
  implicit class SongExtractor(s: Song) {
    lazy val artist = new Artist(s.artistName)
    lazy val release = new Album(s.albumName, s.year, artist)
    lazy val track = new Track(s.title, release)
  }
}
