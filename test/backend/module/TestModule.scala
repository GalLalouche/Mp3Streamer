package backend.module

import java.time.Clock
import java.util.logging.LogManager

import backend.logging.ScribeUtils
import backend.storage.DbProvider
import com.google.inject.Provides
import models.ModelJsonable.SongParser
import models.SongTagParser
import musicfinder.{MusicFinder, PosterLookup}
import net.codingwell.scalaguice.ScalaModule

import common.FakeClock
import common.guice.ModuleUtils
import common.guice.RichModule.richModule
import common.io.{DirectoryRef, MemoryRoot, PathRefFactory, RootDirectory}

private object TestModule extends ScalaModule with ModuleUtils {
  LogManager.getLogManager.readConfiguration(getClass.getResourceAsStream("/logging.properties"))
  override def configure(): Unit = {
    bind[FakeClock].toInstance(new FakeClock)
    bind[DbProvider].toInstance(H2MemProvider.nextNew())
    bind[SongParser].to[MemorySongParser]
    bind[PathRefFactory].to[MemoryPathRefFactory]
    bind[SongTagParser].to[FakeMusicFinder]
    requireBinding[MemoryRoot]
    requireBinding[FakeMusicFinder]
    // TODO this should be a handler!
    // bind[Logger].toInstance(new StringBuilderLogger(new mutable.StringBuilder))
    ScribeUtils.noLogs()

    install(AllModules.overrideWith(TestModule.posterLookup))
  }

  private val posterLookup = new ScalaModule {
    @Provides private def posterLookup(@RootDirectory rootDirectory: DirectoryRef): PosterLookup =
      s => rootDirectory.addFile(s.title + ".poster.jpg")
  }

  // Ensures that the non-fake bindings will be the same as the fake ones.
  @Provides
  private def provideClock(clock: FakeClock): Clock = clock

  @Provides
  @RootDirectory
  private def provideMemoryRoot(directoryRef: MemoryRoot): DirectoryRef = directoryRef

  @Provides
  private def provideMusicFinder(mf: FakeMusicFinder): MusicFinder = mf
}
