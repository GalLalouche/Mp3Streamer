package backend.albums.filler

import scala.concurrent.ExecutionContext

import backend.mb.MbArtistReconciler
import backend.recon.{Artist, ReconID, Reconciler}
import backend.FutureOption
import com.google.inject.Inject
import common.rich.func.BetterFutureInstances.betterFutureInstances
import common.rich.func.RichOptionT.richOptionT

class VerifiedMbArtistReconciler @Inject() (
    artistReconciler: MbArtistReconciler,
    verifier: ArtistReconVerifier,
    ec: ExecutionContext,
) extends Reconciler[Artist] {
  private implicit val iec: ExecutionContext = ec

  override def apply(artist: Artist): FutureOption[ReconID] =
    artistReconciler(artist).mFilterOpt(verifier(artist, _))
}
