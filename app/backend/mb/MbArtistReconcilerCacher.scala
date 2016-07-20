package backend.mb

import backend.recon.ArtistReconcilerCacher
import models.RealLocations
import scala.concurrent.ExecutionContext.Implicits.global

object MbArtistReconcilerCacher extends ArtistReconcilerCacher(ArtistReconStorageImpl,
  new MusicBrainzRetriever()) {
  def main(args: Array[String]) {
    fill(new RealLocations { override val subDirs = List("Rock", "Metal") })
  }
}
