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
        override val subDirNames = Vector("Rock", "Metal")
      }
    ))
    install(existingAlbumsModule)
  }
}
private object LocalNewAlbumsModule extends Debug {
  def forSingleArtist(a: Artist) = new LocalNewAlbumsModule(new ScalaModule {
    @Provides
    @Singleton
    private def existingAlbumsCache(mf: MusicFinder): ExistingAlbums = ExistingAlbums.singleArtist(a, mf)
  })
  def default = new LocalNewAlbumsModule(new ScalaModule {
    @Provides
    @Singleton
    private def existingAlbumsCache(implicit mf: MusicFinder, logger: Logger): ExistingAlbums =
      timed("Creating cache", LoggingLevel.Info) {
        ExistingAlbums.from(mf.genreDirs.view.flatMap(_.deepDirs), mf)
      }
  })
  def overridingStandalone(lnam: LocalNewAlbumsModule): Injector =
    Guice.createInjector(Modules `override` StandaloneModule `with` lnam)
}
