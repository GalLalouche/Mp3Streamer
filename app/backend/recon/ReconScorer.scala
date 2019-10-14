package backend.recon

trait ReconScorer[T <: Reconcilable] extends ((T, T) => Double)

object ReconScorers {
  private val stringMatcher = StringReconScorer
  private def yearMatch(n1: Int, n2: Int): Double =
    if (n1 == n2) 1.1 else if (Math.abs(n1 - n2) == 1) 0.9 else 0.1
  object ArtistReconsScorer extends ReconScorer[Artist] {
    override def apply(a1: Artist, a2: Artist): Double = stringMatcher(a1.name, a2.name)
  }
  object AlbumReconScorer extends ReconScorer[Album] {
    override def apply(a1: Album, a2: Album): Double = Vector(
      ArtistReconsScorer(a1.artist, a2.artist),
      yearMatch(a1.year, a2.year),
      stringMatcher(a1.title, a2.title),
    ).product
  }
}
