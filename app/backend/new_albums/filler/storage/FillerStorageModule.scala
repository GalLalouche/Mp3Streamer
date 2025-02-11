package backend.new_albums.filler.storage

import backend.new_albums.filler.ArtistReconPusherImpl
import backend.recon.ArtistReconPusher
import net.codingwell.scalaguice.ScalaModule

private[new_albums] object FillerStorageModule extends ScalaModule {
  override def configure() = {
    bind[LastFetchTime].to[LastFetchTimeImpl]
    bind[NewAlbumStorage].to[SlickNewAlbumStorage]
    bind[NewAlbumCleaner].to[NewAlbumStorage]
    bind[CachedNewAlbumStorage].to[CachedNewAlbumStorageImpl]
    bind[FilledStorage].to[CachedNewAlbumStorage]
    bind[ArtistReconPusher].to[ArtistReconPusherImpl]
  }
}
