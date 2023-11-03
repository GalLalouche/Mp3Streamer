package controllers

import scala.concurrent.ExecutionContext

import backend.logging.{CompositeLogger, ConsoleLogger, DirectoryLogger, FilteringLogger, Logger, LoggingLevel}
import backend.module.{RealInternetTalkerModule, RealModule}
import com.google.inject.Provides
import common.io.{DirectoryRef, RootDirectory}
import common.rich.RichT._
import net.codingwell.scalaguice.ScalaModule

// Has to be a class for Play to instantiate.
class Module extends ScalaModule {
  override def configure(): Unit = {
    install(RealModule)
    install(RealInternetTalkerModule.nonDaemonic)
  }

  @Provides private def logger(
      @RootDirectory rootDirectory: DirectoryRef,
      ec: ExecutionContext,
  ): Logger = new CompositeLogger(
    (new ConsoleLogger with FilteringLogger).<|(_.setCurrentLevel(LoggingLevel.Verbose)),
    new DirectoryLogger(rootDirectory)(ec),
  )
}
