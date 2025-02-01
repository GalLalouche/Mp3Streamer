package backend.new_albums.filler

import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

import scala.concurrent.ExecutionContext

import common.TimedLogger
import common.rich.RichFuture.richFuture

private object ReconcilableFiller {
  def main(args: Array[String]): Unit = {
    val injector = ExistingAlbumsModules.overridingStandalone(ExistingAlbumsModules.default)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val timed = injector.instance[TimedLogger]
    timed("Filling artist recons", scribe.info(_)) {
      injector.instance[ArtistReconFiller].go().get
    }
    timed("Filling album recons", scribe.info(_)) {
      injector.instance[AlbumReconFiller].go().get
    }
  }
}
