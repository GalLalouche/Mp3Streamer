package backend.configs

import java.time.Clock

import backend.logging.{ConsoleLogger, FilteringLogger, Logger}
import net.codingwell.scalaguice.ScalaModule

object NonPersistentModule extends ScalaModule {
  override def configure(): Unit = {
    bind[Clock] toInstance Clock.systemDefaultZone
    bind[Logger] toInstance new ConsoleLogger with FilteringLogger
  }
}
