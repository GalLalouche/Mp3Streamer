package backend.search

import javax.inject.Inject

import com.google.inject.Singleton
import models.{AlbumDir, ArtistDir, Song}

import scala.concurrent.{ExecutionContext, Future}

import common.concurrency.{UpdatableProxy, UpdatableProxyFactory}

@Singleton
private class SearchState @Inject() (
    index: CompositeIndexFactory,
    proxyFactory: UpdatableProxyFactory,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  private val updater: Future[UpdatableProxy[CompositeIndex]] =
    proxyFactory.initialize(() => index.create())
  def update(): Future[Unit] = updater.flatMap(_.update())

  def search(terms: Seq[String]): Future[(Seq[Song], Seq[AlbumDir], Seq[ArtistDir])] =
    updater.map(_.current.search(terms))
}
