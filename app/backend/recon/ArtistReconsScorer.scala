package backend.recon

import com.google.inject.Inject

class ArtistReconsScorer @Inject() (stringReconScorer: StringReconScorer)
    extends ReconScorer[Artist] {
  override def apply(a1: Artist, a2: Artist): Double = stringReconScorer(a1.name, a2.name)
}
