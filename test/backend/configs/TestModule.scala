package backend.configs

import java.time.Clock

import backend.logging.{Logger, StringBuilderLogger}
import common.FakeClock
import net.codingwell.scalaguice.ScalaModule

class TestModule extends ScalaModule {
  override def configure() = {
    val clock = new FakeClock
    bind[Clock] toInstance clock
    bind[FakeClock] toInstance clock
    bind[Logger] toInstance new StringBuilderLogger(new StringBuilder)
  }
}
