package backend.recon

import common.LanguageString._

object StringReconScorer extends ((String, String) => Double) {
  override def apply(s1: String, s2: String): Double = {
    val (canonS1, canonS2) = if ((s1 ++ s2).hasHebrew) s1 -> s2 else canonize(s1) -> canonize(s2)
    if (sameAndNonEmpty(canonS1, canonS2)) 1 else 0
  }

  private val badWords = Set("and")
  private def canonize(s: String): String =
    s.toLowerCase.split(' ').filterNot(badWords).mkString(" ").filter(_.isLetterOrDigit).keepAscii

  private def sameAndNonEmpty(s1: String, s2: String) = {
    val trim = s1.trim
    trim.nonEmpty && trim == s2.trim
  }
}
