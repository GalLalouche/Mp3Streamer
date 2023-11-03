package backend.logging

import com.google.inject.Module
import net.codingwell.scalaguice.ScalaModule

object LoggingModules {
  val ConsoleWithFiltering: Module = new ScalaModule {
    override def configure() = {
      val logger = new ConsoleLogger with FilteringLogger
      bind[Logger].toInstance(logger)
      bind[FilteringLogger].toInstance(logger)
    }
  }
}
