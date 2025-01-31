package backend.recon

import java.util.Objects

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

/**
 * An Album (contrast with [[models.AlbumDir]]) is the external world entity representing an album.
 * In other words, while an [[models.AlbumDir]] has to physically exist on the filesystem, a release
 * can represent albums which haven't yet been downloaded.
 */
class Album(val title: AlbumTitle, val year: Int, val artist: Artist) extends Reconcilable {
  override def normalize: String = s"${artist.normalize} - ${title.toLowerCase}"
  def toYearless = YearlessAlbum(title, artist)

  override def equals(other: Any): Boolean = other match {
    case that: Album =>
      title.equalsIgnoreCase(that.title) && year == that.year && artist == that.artist
    case _ => false
  }
  override def hashCode(): Int = Objects.hashCode(title.toLowerCase, year, artist)
  override def toString = s"Album($title, $year, $artist)"
}
object Album {
  def apply(title: AlbumTitle, year: Int, artist: Artist) = new Album(title, year, artist)
}

/** An album without a year. Used in places where we want to do comparisons which ignore years. */
class YearlessAlbum(val title: AlbumTitle, val artist: Artist) {
  override def equals(other: Any): Boolean = other match {
    case that: YearlessAlbum =>
      title.equalsIgnoreCase(that.title) && artist == that.artist
    case _ => false
  }
  override def hashCode(): Int = Objects.hashCode(title.toLowerCase, artist)
  override def toString = s"YearlessAlbum($title, $artist)"
}
object YearlessAlbum {
  def apply(title: AlbumTitle, artist: Artist) = new YearlessAlbum(title, artist)
}

class Track(val title: SongTitle, val album: Album) extends Reconcilable {
  private lazy val normalized = title.toLowerCase
  def artist: Artist = album.artist
  def toYearless = YearlessTrack(title, album.toYearless)
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
  def apply(title: SongTitle, album: Album) = new Track(title, album)
}

/**
 * A track whose album is without a year. Used in places where we want to do comparisons which
 * ignore years.
 */
class YearlessTrack(val title: SongTitle, val album: YearlessAlbum) {
  override def equals(other: Any): Boolean = other match {
    case that: YearlessTrack =>
      title.equalsIgnoreCase(that.title) && album == that.album
    case _ => false
  }
  override def hashCode(): Int = Objects.hashCode(title.toLowerCase, album)
  override def toString = s"YearlessTrack($title, $album)"
}
object YearlessTrack {
  def apply(title: SongTitle, album: YearlessAlbum) = new YearlessTrack(title, album)
}

object Reconcilable {
  implicit class SongExtractor(s: Song) {
    lazy val artist = new Artist(s.artistName)
    lazy val release = new Album(s.albumName, s.year, artist)
    lazy val track = new Track(s.title, release)
  }
}
