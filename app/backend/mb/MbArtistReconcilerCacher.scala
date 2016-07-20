package backend.mb

import backend.recon.ArtistReconcilerCacher
import models.RealLocations
import scala.concurrent.ExecutionContext.Implicits.global

object MbArtistReconcilerCacher extends ArtistReconcilerCacher(ArtistReconStorage,
  new MbArtistReconciler()) {
  def main(args: Array[String]) {
    fill(new RealLocations { override val subDirs = List("Rock", "Metal") })
  }
}
