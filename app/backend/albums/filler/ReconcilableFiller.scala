package backend.albums.filler

import backend.logging.{FilteringLogger, LoggingLevel}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

import scala.concurrent.ExecutionContext

import common.rich.RichFuture.richFuture
import common.TimedLogger

private object ReconcilableFiller {
  def main(args: Array[String]): Unit = {
    val injector = ExistingAlbumsModules.overridingStandalone(ExistingAlbumsModules.default)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val timed = injector.instance[TimedLogger]
    injector.instance[FilteringLogger].setCurrentLevel(LoggingLevel.Debug)
    timed("Filling artist recons", LoggingLevel.Info) {
      injector.instance[ArtistReconFiller].go().get
    }
    timed("Filling album recons", LoggingLevel.Info) {
      injector.instance[AlbumReconFiller].go().get
    }
  }
}
