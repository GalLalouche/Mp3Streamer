package backend.recent

import javax.inject.{Inject, Singleton}

import models.AlbumDir

import scala.concurrent.Future

import common.concurrency.{UpdatableProxy, UpdatableProxyFactory}
import common.rich.collections.RichTraversableOnce.richTraversableOnce

@Singleton class LastAlbumState @Inject() (
    factory: UpdatableProxyFactory,
    recentAlbums: RecentAlbums,
) {
  private val updater: UpdatableProxy[Option[AlbumDir]] =
    factory(None, () => Some(recentAlbums.all(1).single), "LastAlbumState")
  def update(): Future[Unit] = updater.update()
  private[recent] def set(albumDir: AlbumDir): Future[Unit] = updater.set(Some(albumDir));
  update()
  def get(): Option[AlbumDir] = updater.current
}
