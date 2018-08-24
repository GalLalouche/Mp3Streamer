package backend.configs

import java.time.Clock

import net.codingwell.scalaguice.ScalaModule

object RealModule extends ScalaModule {
  override def configure(): Unit = {
    bind[Clock] toInstance Clock.systemDefaultZone
  }
}
