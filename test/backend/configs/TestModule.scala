package backend.configs

import java.time.Clock

import backend.logging.{Logger, StringBuilderLogger}
import com.google.inject.Provides
import common.FakeClock
import common.io.{DirectoryRef, MemoryRoot, RootDirectory}
import net.codingwell.scalaguice.ScalaModule

class TestModule extends ScalaModule {
  override def configure() = {
    val clock = new FakeClock
    bind[FakeClock] toInstance clock
    bind[Logger] toInstance new StringBuilderLogger(new StringBuilder)
  }

  @Provides
  def provideClock(clock: FakeClock): Clock = clock

  @Provides // Ensures that if MemoryRoot binding is changed, rootDirectory will remain the same.
  @RootDirectory
  def provideMemoryRoot(@RootDirectory directoryRef: MemoryRoot): DirectoryRef = directoryRef
}
