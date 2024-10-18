package backend.scorer

import javax.inject.{Inject, Singleton}

import backend.recon.{Album, Artist}
import com.google.inject.Provider
import models.Song

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
  override def explicitScore(s: Song) = updatable.current.explicitScore(s)
  override def aggregateScore(f: FileRef) = updatable.current.aggregateScore(f)
  override def fullInfo(s: Song) = updatable.current.fullInfo(s)
}
