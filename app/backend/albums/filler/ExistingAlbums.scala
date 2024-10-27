package backend.albums.filler

import backend.recon.{Album, Artist}

trait ExistingAlbums {
  def artists: Iterable[Artist]
  def albums: Artist => Set[Album]
  def allAlbums: Iterable[Album] = artists.flatMap(albums)
}
