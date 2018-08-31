package backend.configs

import backend.logging.{ConsoleLogger, FilteringLogger, Logger}
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

object StandaloneModule extends ScalaModule {
  override def configure(): Unit = {
    install(AllModules)

    bind[Logger] toInstance new ConsoleLogger with FilteringLogger
    bind[ExecutionContext] toInstance ExecutionContext.Implicits.global

    install(RealInternetTalkerModule.daemonic)
    install(AllModules)
  }
}
