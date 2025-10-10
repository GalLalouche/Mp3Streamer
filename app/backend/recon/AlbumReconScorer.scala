package backend.recon

import com.google.inject.Inject
import org.jline.utils.Levenshtein

import common.rich.primitives.RichDouble.richDouble

class AlbumReconScorer @Inject() (
    stringReconScorer: StringReconScorer,
    artistReconsScorer: ArtistReconsScorer,
) extends ReconScorer[Album] {
  override def apply(a1: Album, a2: Album): Double = Vector(
    artistReconsScorer(a1.artist, a2.artist),
    AlbumReconScorer.yearMatch(a1.year, a2.year),
    math.max(
      stringReconScorer(a1.title, a2.title),
      AlbumReconScorer.levenshteinSimilarity(a1.title, a2.title) ** 0.75,
    ),
  ).product
}

object AlbumReconScorer {
  private def yearMatch(n1: Int, n2: Int): Double =
    if (n1 == n2) 1.1 else math.max(1 - Math.abs(n1 - n2) / 10.0, 0)
  // Prioritizes insertions, to better match common prefix/suffix.
  private def levenshteinSimilarity(s1: String, s2: String): Double = {
    def go(s1: String, s2: String) = {
      val deleteCost = 5
      val insertCost = 1
      val replaceCost = 5
      val swapCost = 5
      val distance = Levenshtein
        .distance(
          s1,
          s2,
          deleteCost,
          insertCost,
          replaceCost,
          swapCost,
        )
        .toDouble / 2.5

      math.max(0.0, 1 - (distance / math.max(s1.length, s2.length)))
    }
    math.max(go(s1, s2), go(s2, s1))
  }
}
