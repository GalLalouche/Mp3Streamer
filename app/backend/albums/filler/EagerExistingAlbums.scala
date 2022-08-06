package backend.albums.filler

import backend.recon.{Album, Artist}
import javax.inject.Singleton

@Singleton private class EagerExistingAlbums(override val albums: Map[Artist, Set[Album]])
    extends ExistingAlbums {
  override def artists: Iterable[Artist] = albums.keys
}
