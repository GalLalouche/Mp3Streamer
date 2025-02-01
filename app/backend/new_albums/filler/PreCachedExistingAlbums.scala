package backend.new_albums.filler

import javax.inject.Singleton

import backend.recon.{Album, Artist}

@Singleton private class PreCachedExistingAlbums(override val albums: Map[Artist, Set[Album]])
    extends ExistingAlbums {
  override def artists: Iterable[Artist] = albums.keys
}
