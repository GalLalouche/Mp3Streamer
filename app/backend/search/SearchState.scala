package backend.search

import javax.inject.Inject
import scala.concurrent.Future

import com.google.inject.Singleton
import common.concurrency.{UpdatableProxy, UpdatableProxyFactory}
import models.{Album, Artist, Song}

@Singleton
private class SearchState @Inject() (
    index: CompositeIndexFactory,
    proxyFactory: UpdatableProxyFactory,
) {
  private val updater: UpdatableProxy[CompositeIndex] =
    proxyFactory.initialize(() => index.create())
  def update(): Future[Unit] = updater.update()

  def search(terms: Seq[String]): (Seq[Song], Seq[Album], Seq[Artist]) =
    updater.current.search(terms)
}
