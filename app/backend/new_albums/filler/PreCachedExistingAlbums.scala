package backend.new_albums.filler

import backend.recon.{Album, Artist}
import com.google.inject.Singleton
import rx.lang.scala.Observable

@Singleton private class PreCachedExistingAlbums(override val albums: Map[Artist, Set[Album]])
    extends ExistingAlbums {
  override def artists: Observable[Artist] = Observable.from(albums.keys)
}
