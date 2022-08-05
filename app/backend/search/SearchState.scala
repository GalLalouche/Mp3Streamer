package backend.search

import com.google.inject.Singleton
import javax.inject.Inject
import models.{Album, Artist, Song}

import scala.concurrent.Future

import common.concurrency.{UpdatableProxy, UpdatableProxyFactory}

@Singleton
private class SearchState @Inject()(index: CompositeIndexFactory, proxyFactory: UpdatableProxyFactory) {
  private val updater: UpdatableProxy[CompositeIndex] = proxyFactory.initialize(() => index.create())
  def update(): Future[Unit] = updater.update()

  def search(terms: Seq[String]): (Seq[Song], Seq[Album], Seq[Artist]) = updater.current search terms
}
