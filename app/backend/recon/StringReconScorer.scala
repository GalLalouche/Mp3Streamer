package backend.recon

import java.text.Normalizer

import common.rich.RichT._

object StringReconScorer extends ((String, String) => Double) {
  private val badWords = Set("and")
  private def normalize(s: String): String =
    Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
  private def canonize(s: String): String =
    s.toLowerCase.split(' ').filterNot(badWords).mkString(" ").filter(_.isLetterOrDigit) |> normalize

  // TODO handle code duplication with StringFixer
  private val hebrewPattern =
    """\p{InHebrew}""".r.unanchored
  private def isHebrew(str: String): Boolean = hebrewPattern.findFirstIn(str).isDefined

  private def sameAndNonEmpty(s1: String, s2: String) = {
    val trim = s1.trim
    trim.nonEmpty && trim == s2.trim
  }
  override def apply(s1: String, s2: String): Double = {
    val same =
      if (isHebrew(s1) || isHebrew(s2)) sameAndNonEmpty(s1, s2)
      else sameAndNonEmpty(canonize(s1), canonize(s2))
    if (same) 1 else 0
  }
}
