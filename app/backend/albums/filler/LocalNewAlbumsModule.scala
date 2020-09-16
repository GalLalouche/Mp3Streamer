package backend.albums.filler

import backend.albums.filler.storage.FillerStorageModule
import backend.logging.{Logger, LoggingLevel, LoggingModules}
import backend.module.StandaloneModule
import backend.recon.Artist
import com.google.inject.{Guice, Injector, Module, Provides, Singleton}
import com.google.inject.util.Modules
import models.{IOMusicFinder, IOMusicFinderModule, MusicFinder}
import net.codingwell.scalaguice.ScalaModule

import common.Debug

private class LocalNewAlbumsModule private(existingAlbumsModule: Module) extends ScalaModule {
  override def configure(): Unit = {
    install(LoggingModules.ConsoleWithFiltering)
    install(new IOMusicFinderModule(
      new IOMusicFinder {
        override val flatGenres = Nil
      }
    ))
    install(existingAlbumsModule)
    install(FillerStorageModule)
  }
}
private object LocalNewAlbumsModule extends Debug {
  def lazyAlbums = new LocalNewAlbumsModule(new ScalaModule {
    override def configure(): Unit = {
      bind[ExistingAlbums].to[LazyExistingAlbums]
    }
  })

  private abstract class EagerBinder extends ScalaModule {
    override def configure(): Unit = {
      bind[ExistingAlbums].to[EagerExistingAlbums]
    }
  }
  def forSingleArtist(artistName: String) = new LocalNewAlbumsModule(new EagerBinder {
    @Provides
    @Singleton
    private def existingAlbumsCache(mf: MusicFinder): EagerExistingAlbums =
      EagerExistingAlbums.singleArtist(Artist(artistName), mf)
  })
  def default = new LocalNewAlbumsModule(new EagerBinder {
    @Provides
    @Singleton
    private def existingAlbumsCache(implicit mf: MusicFinder, logger: Logger): EagerExistingAlbums =
      timed("Creating cache", LoggingLevel.Info) {
        EagerExistingAlbums.from(mf.albumDirs, mf)
      }
  })
  def overridingStandalone(lnam: LocalNewAlbumsModule): Injector =
    Guice.createInjector(Modules `override` StandaloneModule `with` lnam)
}
