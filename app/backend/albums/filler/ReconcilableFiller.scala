package backend.albums.filler

import backend.logging.{Logger, LoggingLevel}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

import scala.concurrent.ExecutionContext

import common.rich.RichFuture.richFuture
import common.Debugging

private object ReconcilableFiller {
  def main(args: Array[String]): Unit = {
    val injector = ExistingAlbumsModules.overridingStandalone(ExistingAlbumsModules.default)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    implicit val logger: Logger = injector.instance[Logger]
    Debugging.timed("Filling artist recons", LoggingLevel.Info) {
      injector.instance[ArtistReconFiller].go().get
    }
    Debugging.timed("Filling album recons", LoggingLevel.Info) {
      injector.instance[AlbumReconFiller].go().get
    }
  }
}
