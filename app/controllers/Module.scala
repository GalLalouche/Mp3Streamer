package controllers

import backend.albums.filler.ExistingAlbumsModules
import backend.logging.{CompositeLogger, ConsoleLogger, DirectoryLogger, FilteringLogger, Logger, LoggingLevel}
import backend.module.{RealInternetTalkerModule, RealModule}
import com.google.inject.util.Modules
import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

import common.io.{DirectoryRef, RootDirectory}
import common.rich.RichT._

// Has to be a class for Play to instantiate.
class Module extends ScalaModule {
  override def configure(): Unit = {
    install(Modules.`override`(RealModule).`with`(ExistingAlbumsModules.lazyAlbums))
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
