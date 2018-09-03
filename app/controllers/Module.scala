package controllers

import backend.logging.{CompositeLogger, ConsoleLogger, DirectoryLogger, FilteringLogger, Logger, LoggingLevel}
import backend.module.{RealInternetTalkerModule, RealModule}
import com.google.inject.Provides
import common.io.{DirectoryRef, RootDirectory}
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

// Has to be a class for Play to instantiate.
class Module extends ScalaModule {
  override def configure(): Unit = {
    install(RealModule)
    install(RealInternetTalkerModule.nonDaemonic)
  }

  @Provides
  private def provideLogger(
      @RootDirectory rootDirectory: DirectoryRef, ec: ExecutionContext): Logger = new CompositeLogger(
    new ConsoleLogger with FilteringLogger {setCurrentLevel(LoggingLevel.Verbose)},
    new DirectoryLogger(rootDirectory)(ec),
  )
}
