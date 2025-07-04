package backend.score

import backend.recon.{Album, Artist, Track}
import com.google.inject.{Inject, Provider, Singleton}

import scala.concurrent.ExecutionContext

import common.concurrency.{UpdatableProxy, UpdatableProxyFactory}
import common.io.FileRef
import common.rich.RichFuture.richFuture

@Singleton private class CachedModelScorerState @Inject() (
    provider: Provider[CachedModelScorerImpl],
    factory: UpdatableProxyFactory,
    ec: ExecutionContext,
) extends CachedModelScorer {
  private implicit val iec: ExecutionContext = ec
  // TODO unblock. This is actually harder than it seems, since the entire point of
  //  CachedModelScorer is that it *isn't* async!
  private lazy val updatable: UpdatableProxy[CachedModelScorer] =
    factory.initialize[CachedModelScorer](provider.get).get
  def update() = updatable.update()

  override def explicitScore(a: Artist) = updatable.current.explicitScore(a)
  override def explicitScore(a: Album) = updatable.current.explicitScore(a)
  override def explicitScore(s: Track) = updatable.current.explicitScore(s)
  override def aggregateScore(f: FileRef) = updatable.current.aggregateScore(f)
  override def fullInfo(s: Track) = updatable.current.fullInfo(s)
}
