package backend.albums.filler.storage

import net.codingwell.scalaguice.ScalaModule

private[albums] object FillerStorageModule extends ScalaModule {
  override def configure() = {
    bind[LastFetchTime].to[LastFetchTimeImpl]
    bind[NewAlbumStorage].to[SlickNewAlbumStorage]
    bind[CachedNewAlbumStorage].to[CachedNewAlbumStorageImpl]
    bind[FilledStorage].to[CachedNewAlbumStorage]
  }
}
