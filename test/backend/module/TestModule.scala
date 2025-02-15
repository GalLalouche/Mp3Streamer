package backend.module

import java.time.Clock
import java.util.logging.LogManager

import backend.logging.ScribeUtils
import com.google.inject.Provides
import musicfinder.MusicFinder
import net.codingwell.scalaguice.ScalaModule

import common.FakeClock
import common.guice.ModuleUtils
import common.io.{DirectoryRef, MemoryRoot, RootDirectory}

class TestModule extends ScalaModule with ModuleUtils {
  LogManager.getLogManager.readConfiguration(getClass.getResourceAsStream("/logging.properties"))
  override def configure() = {
    bind[FakeClock].toInstance(new FakeClock)
    // TODO this should be a handler!
    // bind[Logger].toInstance(new StringBuilderLogger(new mutable.StringBuilder))
    ScribeUtils.noLogs()

    requireBinding[MemoryRoot]
    requireBinding[FakeMusicFinder]

    install(AllModules)
  }

  // Ensures that the non-fake bindings will be the same as the fake ones.
  @Provides
  private def provideClock(clock: FakeClock): Clock = clock

  @Provides
  @RootDirectory
  private def provideMemoryRoot(@RootDirectory directoryRef: MemoryRoot): DirectoryRef =
    directoryRef

  @Provides
  private def provideMusicFinder(mf: FakeMusicFinder): MusicFinder = mf
}
