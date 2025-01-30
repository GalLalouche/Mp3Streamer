package models

final case class Artist(name: ArtistName, private val _albums: Set[Album]) {
  lazy val albums: Seq[Album] = _albums.toSeq.sortBy(e => (e.year, e.title))
}
