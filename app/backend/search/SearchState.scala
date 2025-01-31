package backend.search

import javax.inject.Inject

import com.google.inject.Singleton
import models.{AlbumDir, ArtistDir, Song}

import scala.concurrent.Future

import common.concurrency.{UpdatableProxy, UpdatableProxyFactory}

@Singleton
private class SearchState @Inject() (
    index: CompositeIndexFactory,
    proxyFactory: UpdatableProxyFactory,
) {
  private val updater: UpdatableProxy[CompositeIndex] =
    proxyFactory.initialize(() => index.create())
  def update(): Future[Unit] = updater.update()

  def search(terms: Seq[String]): (Seq[Song], Seq[AlbumDir], Seq[ArtistDir]) =
    updater.current.search(terms)
}
