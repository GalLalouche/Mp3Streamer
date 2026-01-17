package backend.new_albums.filler

import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

import common.TimedLogger
import common.rich.RichFuture.richFutureBlocking

private object ReconcilableFiller {
  def main(args: Array[String]): Unit = {
    val injector = ExistingAlbumsModules.overridingStandalone(ExistingAlbumsModules.default)
    val timed = injector.instance[TimedLogger]
    timed("Filling artist recons", scribe.info(_)) {
      injector.instance[ArtistReconFiller].go().get
    }
    timed("Filling album recons", scribe.info(_)) {
      injector.instance[AlbumReconFiller].go().get
    }
  }
}
