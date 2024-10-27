package backend.scorer

import javax.inject.{Inject, Singleton}

import backend.recon.{Album, Artist, Track}
import com.google.inject.Provider

import common.concurrency.{UpdatableProxy, UpdatableProxyFactory}
import common.io.FileRef

@Singleton private class CachedModelScorerState @Inject() (
    provider: Provider[CachedModelScorerImpl],
    factory: UpdatableProxyFactory,
) extends CachedModelScorer {
  private val updatable: UpdatableProxy[CachedModelScorer] = factory.initialize(provider.get)
  def update() = updatable.update()

  override def explicitScore(a: Artist) = updatable.current.explicitScore(a)
  override def explicitScore(a: Album) = updatable.current.explicitScore(a)
  override def explicitScore(s: Track) = updatable.current.explicitScore(s)
  override def aggregateScore(f: FileRef) = updatable.current.aggregateScore(f)
  override def fullInfo(s: Track) = updatable.current.fullInfo(s)
}
