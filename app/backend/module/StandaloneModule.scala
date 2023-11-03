package backend.module

import scala.concurrent.ExecutionContext
import scala.util.Random

import backend.logging.LoggingModules
import net.codingwell.scalaguice.ScalaModule

class StandaloneModule(random: Random) extends ScalaModule {
  override def configure(): Unit = {
    bind[ExecutionContext].toInstance(ExecutionContext.Implicits.global)
    bind[Random].toInstance(random)

    install(RealInternetTalkerModule.daemonic)
    install(RealModule)
    install(AllModules)
    install(LoggingModules.ConsoleWithFiltering)
  }
}
object StandaloneModule extends StandaloneModule(Random)
