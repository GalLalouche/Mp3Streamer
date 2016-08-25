package backend.recon

import java.text.Normalizer

import common.rich.RichT._

object StringReconScorer extends ((String, String) => Double) {
  private val removeChars = ".:-_, ;".toSet
  private def normalize(s: String): String =
    Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
  private def canonize(s: String): String =
    s.toLowerCase.filterNot(removeChars.contains) |> normalize
  override def apply(s1: String, s2: String): Double = {
    val c1 = canonize(s1)
    val c2 = canonize(s2)
    if (c1 == c2)
      1.0
    else
      0.0
  }
}
