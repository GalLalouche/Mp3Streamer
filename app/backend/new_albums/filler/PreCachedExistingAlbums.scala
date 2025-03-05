package backend.new_albums.filler

import backend.recon.{Album, Artist}
import com.google.inject.Singleton

@Singleton private class PreCachedExistingAlbums(override val albums: Map[Artist, Set[Album]])
    extends ExistingAlbums {
  override def artists: Iterable[Artist] = albums.keys
}
