package backend.recon

import com.google.inject.Inject
import mains.fixer.StringFixer

import common.LanguageString._
import common.rich.RichT.richT
import common.rich.RichTuple._
import common.rich.primitives.RichString.richString

/**
 * A placeholder for a class that might some day in the future actually measure the similarity
 * between two strings. Now it just removes some stop words and punctuations and checks if the
 * strings are identical.
 */
class StringReconScorer @Inject() (stringFixer: StringFixer) extends ((String, String) => Double) {
  import StringReconScorer._

  override def apply(s1: String, s2: String): Double = {
    val (canonS1, canonS2) = (s1 -> s2).map(if ((s1 ++ s2).hasHebrew) filterHebrew else canonize)
    if (sameAndNonEmpty(canonS1, canonS2)) 1 else 0
  }

  private def canonize(s: String): String =
    s.toLowerCase
      .|>(stringFixer.withoutSpecialCharacters)
      .tokenize(Tokens)
      .filterNot(BadWords)
      .mkString(" ")
      .filter(_.isLetterOrDigit)
      .tryOrKeep(stringFixer.asciiNormalize)

  private def filterHebrew(s: String): String = s.split(" ").filter(_.hasHebrew).mkString(" ")
}

object StringReconScorer {
  val Tokens = "!?,. "
  private val BadWords = Set("and", "ep")
  private def sameAndNonEmpty(s1: String, s2: String) = {
    val trim = s1.trim
    trim.nonEmpty && trim == s2.trim
  }
}
