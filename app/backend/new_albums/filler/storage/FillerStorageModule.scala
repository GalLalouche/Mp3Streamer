package backend.new_albums.filler.storage

import backend.new_albums.filler.ArtistReconPusherImpl
import backend.recon.{ArtistReconPusher, ReconID}
import net.codingwell.scalaguice.ScalaModule

import common.storage.Storage

private[new_albums] object FillerStorageModule extends ScalaModule {
  override def configure() = {
    bind[LastFetchTime].to[LastFetchTimeImpl]
    bind[NewAlbumStorage].to[SlickNewAlbumStorage]
    bind[Storage[ReconID, StoredNewAlbum]].to[NewAlbumStorage]
    bind[NewAlbumCleaner].to[NewAlbumStorage]
    bind[CachedNewAlbumStorage].to[CachedNewAlbumStorageImpl]
    bind[FilledStorage].to[CachedNewAlbumStorage]
    bind[ArtistReconPusher].to[ArtistReconPusherImpl]
  }
}
