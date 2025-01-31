package models

final case class Artist(name: ArtistName, private val _albums: Set[AlbumDir]) {
  lazy val albums: Seq[AlbumDir] = _albums.toSeq.sortBy(e => (e.year, e.title))
}
