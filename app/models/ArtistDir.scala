package models

import backend.recon.Artist

import monocle.Lens

import common.io.DirectoryRef

/**
 * An artist directory (contrast with [[backend.recon.Artist]]) is a concrete directory containing
 * album directories (or, in rare cases, it might be a single album artist whose artist directory
 * contains the song files directly). In other words, an ArtistDir has to physically exist on the
 * filesystem, as well as all of its albums.
 */
final case class ArtistDir(
    dir: DirectoryRef,
    name: ArtistName,
    private val _albums: Set[AlbumDir],
) {
  lazy val albums: Seq[AlbumDir] = _albums.toSeq.sortBy(e => (e.year, e.title))
  def toRecon: Artist = Artist(name)
}
object ArtistDir {
  val albums: Lens[ArtistDir, Seq[AlbumDir]] = Lens[ArtistDir, Seq[AlbumDir]](_.albums)(albums =>
    artist => new ArtistDir(artist.dir, artist.name, albums.toSet),
  )
}
