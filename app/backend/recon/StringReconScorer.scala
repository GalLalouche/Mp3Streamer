package backend.recon

import common.rich.RichTuple._
import common.LanguageString._

/**
* A placeholder for a class that might some day in the future actually measure the similarity between two
* strings. Now it just removes some stop words and punctuations and checks if the strings are identical.
*/
object StringReconScorer extends ((String, String) => Double) {
  override def apply(s1: String, s2: String): Double = {
    val (canonS1, canonS2) = (s1 -> s2).map(if ((s1 ++ s2).hasHebrew) filterHebrew else canonize)
    if (sameAndNonEmpty(canonS1, canonS2)) 1 else 0
  }

  private val badWords = Set("and", "ep")
  private def canonize(s: String): String =
    s.toLowerCase.split(' ').filterNot(badWords).mkString(" ").filter(_.isLetterOrDigit).keepAscii

  private def filterHebrew(s: String): String = s.split(" ").filter(_.hasHebrew).mkString(" ")
  private def sameAndNonEmpty(s1: String, s2: String) = {
    val trim = s1.trim
    trim.nonEmpty && trim == s2.trim
  }
}
