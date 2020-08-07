package backend.albums.filler

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
  }
}
private object LocalNewAlbumsModule extends Debug {
  def lazyAlbums = new LocalNewAlbumsModule(new ScalaModule {
    override def configure(): Unit = {
      bind[ExistingAlbums].to[LazyExistingAlbums]
    }
  })

  def forSingleArtist(a: Artist) = new LocalNewAlbumsModule(new ScalaModule {
    @Provides
    @Singleton
    private def existingAlbumsCache(mf: MusicFinder): EagerExistingAlbums =
      EagerExistingAlbums.singleArtist(a, mf)
  })
  def default = new LocalNewAlbumsModule(new ScalaModule {
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
