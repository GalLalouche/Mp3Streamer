package backend.mb

import backend.recon.{ArtistReconStorage, ArtistReconcilerCacher}
import models.RealLocations

import scala.concurrent.ExecutionContext

class MbArtistReconcilerCacher(implicit ec: ExecutionContext)
  extends ArtistReconcilerCacher(new ArtistReconStorage, new MbArtistReconciler()) {
  def main(args: Array[String]) {
    fill(new RealLocations { override val subDirs = List("Rock", "Metal") })
  }
}
