package backend.new_albums.filler

import backend.recon.{Album, Artist}

private trait ExistingAlbums {
  def artists: Iterable[Artist]
  def albums: Artist => Set[Album]
  def allAlbums: Iterable[Album] = artists.flatMap(albums)
}
