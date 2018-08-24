package backend.configs

import backend.logging.{ConsoleLogger, FilteringLogger, Logger}
import net.codingwell.scalaguice.ScalaModule

object StandaloneModule extends ScalaModule {
  override def configure(): Unit = {
    bind[Logger] toInstance new ConsoleLogger with FilteringLogger
  }
}
