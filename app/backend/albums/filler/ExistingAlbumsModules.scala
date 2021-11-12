package backend.albums.filler

import java.time.Clock

import backend.albums.filler.storage.FillerStorageModule
import backend.logging.{Logger, LoggingLevel, LoggingModules}
import backend.module.{CleanModule, StandaloneModule}
import backend.recon.Artist
import com.google.inject.{Guice, Injector, Module, Provides, Singleton}
import com.google.inject.util.Modules
import models.{IOMusicFinderModule, MusicFinder}
import net.codingwell.scalaguice.ScalaModule

import common.Debugging

private[albums] object ExistingAlbumsModules {
  private[filler] def lazyAlbums: Module = new ScalaModule {
    override def configure(): Unit = {
      bind[ExistingAlbums].to[LazyExistingAlbums]
    }
  }

  private abstract class EagerBinder extends ScalaModule {
    override def configure(): Unit = {
      bind[ExistingAlbums].to[EagerExistingAlbums]
    }
  }
  private[filler] def forSingleArtist(artistName: String): Module = new EagerBinder {
    @Provides @Singleton private def existingAlbumsCache(
        factory: EagerExistingAlbumsFactory): EagerExistingAlbums = factory.singleArtist(Artist(artistName))
  }
  def default: Module = new EagerBinder {
    @Provides @Singleton private def existingAlbumsCache(
        implicit factory: EagerExistingAlbumsFactory, mf: MusicFinder, logger: Logger): EagerExistingAlbums =
      Debugging.timed("Creating cache", LoggingLevel.Info) {
        factory.from(ExistingAlbums.albumDirectories(mf))
      }
  }
  private def overriding(overridenModule: Module)(existingAlbumsModule: Module): Injector = {
    Guice.createInjector(Modules `override` overridenModule `with` new ScalaModule {
      override def configure(): Unit = {
        install(existingAlbumsModule)
        install(LoggingModules.ConsoleWithFiltering)
        install(IOMusicFinderModule)
        install(FillerStorageModule)
      }
    })
  }
  private[filler] def overridingStandalone: Module => Injector = overriding(StandaloneModule)
  // For debugging
  private[filler] def overridingClean: Module => Injector = overriding(CleanModule)
}
