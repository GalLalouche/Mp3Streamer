package backend.recon

import models.{Song, SongTitle}
import models.TypeAliases.{AlbumTitle, ArtistName}
import org.typelevel.ci.CIString

sealed trait Reconcilable {
  def normalize: String
  def artist: Artist
}
/**
 * An Artist (contrast with [[models.ArtistDir]]) is the external world entity representing an
 * artist. In other words, while an [[models.ArtistDir]] has to physically exist on the filesystem,
 * an artist can represent artists which don't have any albums or directories downloaded.
 */
case class Artist(private val _name: CIString) extends Reconcilable {
  def name: ArtistName = _name.toString
  override def normalize: String = name.toLowerCase
  override val artist: Artist = this
}
object Artist {
  def apply(name: ArtistName) = new Artist(CIString(name))
}

/**
 * An Album (contrast with [[models.AlbumDir]]) is the external world entity representing an album.
 * In other words, while an [[models.AlbumDir]] has to physically exist on the filesystem, a release
 * can represent albums which haven't yet been downloaded.
 */
case class Album(private val _title: CIString, year: Int, override val artist: Artist)
    extends Reconcilable {
  def title: AlbumTitle = _title.toString
  override def normalize: String = s"${artist.normalize} - ${title.toLowerCase}"
  def toYearless = YearlessAlbum(_title, artist)
}
object Album {
  def apply(title: AlbumTitle, year: Int, artist: Artist) = new Album(CIString(title), year, artist)
}

/** An album without a year. Used in places where we want to do comparisons which ignore years. */
case class YearlessAlbum(private val _title: CIString, artist: Artist) {
  def title: AlbumTitle = _title.toString
}
object YearlessAlbum {
  def apply(title: AlbumTitle, artist: Artist) = new YearlessAlbum(CIString(title), artist)
}

case class Track(_title: CIString, album: Album) extends Reconcilable {
  def title: SongTitle = _title.toString
  override def artist: Artist = album.artist
  def toYearless = new YearlessTrack(_title, album.toYearless)
  override def normalize: String = ???
}
object Track {
  def apply(title: SongTitle, album: Album) = new Track(CIString(title), album)
}

/**
 * A track whose album is without a year. Used in places where we want to do comparisons which
 * ignore years.
 */
case class YearlessTrack(_title: CIString, album: YearlessAlbum) {
  def title: SongTitle = _title.toString
  def artist: Artist = album.artist
}
object YearlessTrack {
  def apply(title: SongTitle, album: YearlessAlbum) = new YearlessTrack(CIString(title), album)
}

object Reconcilable {
  implicit class SongExtractor(private val $ : Song) extends AnyVal {
    def artist: Artist = Artist($.artistName)
    def release: Album = Album($.albumName, $.year, artist)
    def track: Track = Track($.title, release)
  }
}
