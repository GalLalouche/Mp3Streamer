package backend.module

import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext
import scala.util.Random

class StandaloneModule(random: Random) extends ScalaModule {
  override def configure(): Unit = {
    bind[ExecutionContext].toInstance(ExecutionContext.Implicits.global)
    bind[Random].toInstance(random)

    install(RealInternetTalkerModule.daemonic)
    install(RealModule)
  }
}
object StandaloneModule extends StandaloneModule(Random)
