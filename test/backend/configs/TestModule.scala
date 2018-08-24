package backend.configs

import java.time.Clock

import common.FakeClock
import net.codingwell.scalaguice.ScalaModule

class TestModule extends ScalaModule {
  override def configure() = {
    val clock = new FakeClock
    bind[Clock] toInstance clock
    bind[FakeClock] toInstance clock
  }
}
