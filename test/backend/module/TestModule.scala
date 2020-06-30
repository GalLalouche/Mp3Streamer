package backend.module

import java.time.Clock

import backend.logging.{Logger, StringBuilderLogger}
import com.google.inject.Provides
import models.MusicFinder
import net.codingwell.scalaguice.ScalaModule

import common.FakeClock
import common.guice.ModuleUtils
import common.io.{DirectoryRef, MemoryRoot, RootDirectory}

class TestModule extends ScalaModule with ModuleUtils {
  override def configure() = {
    bind[FakeClock] toInstance new FakeClock
    bind[Logger] toInstance new StringBuilderLogger(new StringBuilder)

    requireBinding[MemoryRoot]
    requireBinding[FakeMusicFinder]

    install(AllModules)
  }

  // Ensures that the non-fake bindings will be the same as the fake ones.
  @Provides
  private def provideClock(clock: FakeClock): Clock = clock

  @Provides
  @RootDirectory
  private def provideMemoryRoot(@RootDirectory directoryRef: MemoryRoot): DirectoryRef = directoryRef

  @Provides
  private def provideMusicFinder(mf: FakeMusicFinder): MusicFinder = mf
}
