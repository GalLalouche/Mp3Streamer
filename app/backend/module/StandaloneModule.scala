package backend.module

import backend.logging.LoggingModules
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

object StandaloneModule extends ScalaModule {
  override def configure(): Unit = {
    bind[ExecutionContext] toInstance ExecutionContext.Implicits.global

    install(RealInternetTalkerModule.daemonic)
    install(RealModule)
    install(AllModules)
    install(LoggingModules.ConsoleWithFiltering)
  }
}
