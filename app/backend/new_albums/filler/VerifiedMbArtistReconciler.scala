package backend.new_albums.filler

import backend.FutureOption
import backend.mb.ArtistReconciler
import backend.recon.{Artist, Reconciler, ReconID}
import com.google.inject.Inject

import scala.concurrent.ExecutionContext

/**
 * Reconciles artists, with an added verification that the downloaded album match those in
 * MusicBrainz, thus avoiding false positives.
 */
class VerifiedMbArtistReconciler @Inject() (
    artistReconciler: ArtistReconciler,
    verifier: ArtistReconVerifier,
    ec: ExecutionContext,
) extends Reconciler[Artist] {
  private implicit val iec: ExecutionContext = ec

  override def apply(artist: Artist): FutureOption[ReconID] =
    artistReconciler(artist).filterF(verifier(artist, _))
}
