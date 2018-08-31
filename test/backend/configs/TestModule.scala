package backend.configs

import java.time.Clock

import backend.logging.{Logger, StringBuilderLogger}
import com.google.inject.Provides
import common.{FakeClock, MyScalaModule}
import common.io.{DirectoryRef, MemoryRoot, RootDirectory}
import models.MusicFinder

class TestModule extends MyScalaModule {
  override def configure() = {
    val clock = new FakeClock
    bind[FakeClock] toInstance clock
    bind[Logger] toInstance new StringBuilderLogger(new StringBuilder)

    // TODO make a pullrequest to fix this in scalaguice?
    requireBinding[MemoryRoot]
    requireBinding[FakeMusicFinder]
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
