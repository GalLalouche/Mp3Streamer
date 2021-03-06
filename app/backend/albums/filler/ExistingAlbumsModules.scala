package backend.albums.filler

import java.time.Clock

import backend.albums.filler.storage.FillerStorageModule
import backend.logging.{Logger, LoggingLevel, LoggingModules}
import backend.module.StandaloneModule
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
        mf: MusicFinder, clock: Clock, logger: Logger): EagerExistingAlbums =
      EagerExistingAlbums.singleArtist(Artist(artistName), mf, clock, logger)
  }
  def default: Module = new EagerBinder {
    @Provides @Singleton private def existingAlbumsCache(
        implicit mf: MusicFinder, logger: Logger, clock: Clock): EagerExistingAlbums =
      Debugging.timed("Creating cache", LoggingLevel.Info) {
        EagerExistingAlbums.from(ExistingAlbums.albumDirectories(mf), mf, clock, logger)
      }
  }
  private[filler] def overridingStandalone(m: Module): Injector =
    Guice.createInjector(Modules `override` StandaloneModule `with` new ScalaModule {
      override def configure(): Unit = {
        install(m)
        install(LoggingModules.ConsoleWithFiltering)
        install(IOMusicFinderModule)
        install(FillerStorageModule)
      }
    })
}
