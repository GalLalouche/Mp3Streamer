package backend.score

import backend.recon.{Album, Artist, Track}
import com.google.inject.{Inject, Provider, Singleton}

import scala.concurrent.{ExecutionContext, Future}

import cats.implicits.toFunctorOps

import common.concurrency.{UpdatableProxy, UpdatableProxyFactory}
import common.io.FileRef
import common.rich.RichFuture.richFuture

@Singleton private class CachedModelScorerState @Inject() (
    provider: Provider[CachedModelScorerImpl],
    factory: UpdatableProxyFactory,
    ec: ExecutionContext,
) extends AggregateScorer
    with IndividualScorer
    with FullInfoScorer {
  private implicit val iec: ExecutionContext = ec
  // TODO unblock. This is actually harder than it seems, since the entire point of
  //  CachedModelScorer is that it *isn't* async!
  private lazy val updatable: UpdatableProxy[CachedModelScorerImpl] =
    factory.initialize[CachedModelScorerImpl](provider.get).get
  def update(): Future[Unit] = updatable.update().void

  override def explicitScore(a: Artist) = updatable.get.explicitScore(a)
  override def explicitScore(a: Album) = updatable.get.explicitScore(a)
  override def explicitScore(s: Track) = updatable.get.explicitScore(s)
  override def aggregateScore(f: FileRef) = updatable.get.aggregateScore(f)
  override def aggregateScore(s: Track): SourcedOptionalModelScore =
    fullInfo(s).sourcedOptionalModelScore
  override def fullInfo(s: Track) = updatable.get.fullInfo(s)
}
