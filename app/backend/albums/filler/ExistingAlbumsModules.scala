package backend.albums.filler

import backend.albums.filler.storage.FillerStorageModule
import backend.logging.{LoggingLevel, LoggingModules}
import backend.module.{CleanModule, StandaloneModule}
import backend.recon.{Artist, ReconcilableFactory}
import com.google.inject.{Guice, Injector, Module, Provides, Singleton}
import common.guice.RichModule.richModule
import common.TimedLogger
import models.IOMusicFinderModule
import net.codingwell.scalaguice.ScalaModule

object ExistingAlbumsModules {
  def lazyAlbums: Module = new ScalaModule {
    override def configure(): Unit =
      bind[ExistingAlbums].to[LazyExistingAlbums]
  }

  private abstract class EagerBinder extends ScalaModule {
    override def configure(): Unit =
      bind[ExistingAlbums].to[EagerExistingAlbums]
  }
  def forSingleArtist(artistName: String): Module = new EagerBinder {
    @Provides @Singleton private def existingAlbumsCache(
        factory: EagerExistingAlbumsFactory,
    ): EagerExistingAlbums = factory.singleArtist(Artist(artistName))
  }
  def default: Module = new EagerBinder {
    @Provides @Singleton private def existingAlbumsCache(
        factory: EagerExistingAlbumsFactory,
        timed: TimedLogger,
        reconcilableFactory: ReconcilableFactory,
    ): EagerExistingAlbums = timed("Creating cache", LoggingLevel.Info) {
      factory.from(reconcilableFactory.albumDirectories)
    }
  }
  private def overriding(overridenModule: Module)(existingAlbumsModule: Module): Injector =
    Guice.createInjector(overridenModule.overrideWith(new ScalaModule {
      override def configure(): Unit = {
        install(existingAlbumsModule)
        install(LoggingModules.ConsoleWithFiltering)
        install(IOMusicFinderModule)
        install(FillerStorageModule)
      }
    }))
  private[filler] def overridingStandalone: Module => Injector = overriding(StandaloneModule)
  // For debugging
  private[filler] def overridingClean: Module => Injector = overriding(CleanModule)
}
