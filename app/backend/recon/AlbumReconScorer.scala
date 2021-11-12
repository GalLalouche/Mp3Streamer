package backend.recon

import javax.inject.Inject

class AlbumReconScorer @Inject()(
    stringReconScorer: StringReconScorer,
    artistReconsScorer: ArtistReconsScorer,
) extends ReconScorer[Album] {
  override def apply(a1: Album, a2: Album): Double = Vector(
    artistReconsScorer(a1.artist, a2.artist),
    AlbumReconScorer.yearMatch(a1.year, a2.year),
    stringReconScorer(a1.title, a2.title),
  ).product
}

object AlbumReconScorer {
  private def yearMatch(n1: Int, n2: Int): Double =
    if (n1 == n2) 1.1 else if (Math.abs(n1 - n2) == 1) 0.9 else 0.1
}