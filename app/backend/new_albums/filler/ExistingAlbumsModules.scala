package backend.new_albums.filler

import backend.module.{CleanModule, IOSongsModule, StandaloneModule}
import backend.new_albums.DirectoryDiscovery
import backend.new_albums.filler.storage.FillerStorageModule
import backend.recon.Artist
import com.google.inject.{Guice, Injector, Module, Provides, Singleton}
import models.TypeAliases.ArtistName
import net.codingwell.scalaguice.ScalaModule

import common.TimedLogger
import common.guice.RichModule.richModule

object ExistingAlbumsModules {
  def lazyAlbums: Module = new ScalaModule {
    override def configure(): Unit =
      bind[ExistingAlbums].to[RealTimeExistingAlbums]
  }

  private abstract class EagerBinder extends ScalaModule {
    override def configure(): Unit =
      bind[ExistingAlbums].to[PreCachedExistingAlbums]
  }
  def forSingleArtist(name: ArtistName): Module = new EagerBinder {
    @Provides @Singleton private def existingAlbumsCache(
        factory: PreCachedExistingAlbumsFactory,
    ): PreCachedExistingAlbums = factory.singleArtist(Artist(name))
  }
  def default: Module = new EagerBinder {
    @Provides @Singleton private def existingAlbumsCache(
        factory: PreCachedExistingAlbumsFactory,
        timed: TimedLogger,
        directoryDiscovery: DirectoryDiscovery,
    ): PreCachedExistingAlbums = timed("Creating cache", scribe.info(_)) {
      factory.from(directoryDiscovery.albumDirectories)
    }
  }
  private def overriding(overridenModule: Module)(existingAlbumsModule: Module): Injector =
    Guice.createInjector(overridenModule.overrideWith(new ScalaModule {
      override def configure(): Unit = {
        install(existingAlbumsModule)
        install(IOSongsModule)
        install(FillerStorageModule)
      }
    }))
  private[filler] def overridingStandalone: Module => Injector = overriding(StandaloneModule)
  // For debugging
  private[filler] def overridingClean: Module => Injector = overriding(CleanModule)
}
