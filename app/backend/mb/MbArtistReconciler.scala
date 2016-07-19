package backend.mb

import backend.recon.ArtistReconciler
import models.RealLocations
import scala.concurrent.ExecutionContext.Implicits.global

object MbArtistReconciler extends ArtistReconciler(ArtistReconStorageImpl, new MusicBrainzRetriever()) {
  def main(args: Array[String]) {
    fill(new RealLocations { override val subDirs = List("Rock", "Metal") })
  }
}
