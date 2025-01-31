package models

/**
 * An artist directory (contrast with [[backend.recon.Artist]]) is a concrete directory containing
 * album directories (or, in rare cases, it might be a single album artist whose artist directory
 * contains the song files directly). In other words, an ArtistDir has to physically exist on the
 * filesystem, as well as all of its albums.
 */
final case class ArtistDir(name: ArtistName, private val _albums: Set[AlbumDir]) {
  lazy val albums: Seq[AlbumDir] = _albums.toSeq.sortBy(e => (e.year, e.title))
}
