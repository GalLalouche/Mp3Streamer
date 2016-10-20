package backend.recon

import java.text.Normalizer

import common.rich.RichT._

object StringReconScorer extends ((String, String) => Double) {
  private val badWords = Set("and")
  private def normalize(s: String): String =
    Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
  private def canonize(s: String): String =
    s.toLowerCase.split(' ').filterNot(badWords).mkString(" ").filter(_.isLetterOrDigit) |> normalize
  override def apply(s1: String, s2: String): Double = if (canonize(s1) == canonize(s2)) 1.0 else 0.0
}
