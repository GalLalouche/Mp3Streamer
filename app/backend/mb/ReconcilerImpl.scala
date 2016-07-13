package backend.mb

import backend.recon.ArtistReconciler
import models.RealLocations
import scala.concurrent.ExecutionContext.Implicits.global

object ReconcilerImpl extends ArtistReconciler(ReconStorageImpl, MusicBrainzRetriever) {
  def main(args: Array[String]) {
    fill(new RealLocations { override val subDirs = List("Rock", "Metal") })
  }
}
