package backend.scorer

import backend.recon.{Album, Artist}
import com.google.inject.Provider
import javax.inject.{Inject, Singleton}
import models.Song

import common.concurrency.{UpdatableProxy, UpdatableProxyFactory}
import common.io.FileRef

@Singleton private class CachedModelScorerState @Inject() (
    provider: Provider[CachedModelScorerImpl],
    factory: UpdatableProxyFactory,
) extends CachedModelScorer {
  private val updatable: UpdatableProxy[CachedModelScorer] = factory.initialize(provider.get)
  def update() = updatable.update()

  override def apply(a: Artist) = updatable.current.apply(a)
  override def apply(a: Album) = updatable.current.apply(a)
  override def apply(s: Song) = updatable.current.apply(s)
  override def apply(f: FileRef) = updatable.current.apply(f)
  override def fullInfo(s: Song) = updatable.current.fullInfo(s)
}
