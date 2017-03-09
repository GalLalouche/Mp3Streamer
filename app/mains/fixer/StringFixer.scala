package mains.fixer

import java.text.Normalizer

import common.rich.RichT._

object StringFixer {
  private[fixer] val lowerCaseWords = List("a", "ain't", "all", "am", "an", "and", "are", "aren't", "as", "at", "be", "but", "by", "can", "can't",
    "cannot", "did", "didn't", "do", "doesn't", "don't", "for", "from", "get", "got", "gotten", "had", "has", "have", "her", "his", "in", "into", "is",
    "isn't", "it", "it's", "its", "may", "me", "mine", "my", "not", "of", "on", "or", "our", "ours", "ov", "shall", "should", "so", "than",
    "that", "the", "their", "theirs", "them", "then", "there", "these", "this", "those", "through", "to", "too", "up", "upon", "was", "wasn't",
    "were", "weren't", "will", "with", "without", "won't", "would", "wouldn't", "your")
  private val lowerCaseSet = lowerCaseWords.toSet
  private val delimiters = " ()-:/\\".toSet

  private def pascalCaseWord(w: String): String = w.toLowerCase.capitalize

  private def fixWord(word: String, isFirstWord: Boolean): String = word match {
    case e if !isFirstWord && lowerCaseSet(e.toLowerCase) => e.toLowerCase
    case e if word.matches(".*[A-Z].*") => e // mixed caps
    case e if e.head.isDigit => e.toLowerCase // 1st, 2nd, etc.
    case s if s matches "[IVXMLivxml]+" => s.toUpperCase // roman numbers, also handles pronoun "I"
    case s if s matches "(\\w\\.)+" => s.toUpperCase // A.B.C. pattern
    case _ => word |> pascalCaseWord
  }

  private def splitWithDelimiters(s: String): List[String] =
    s.foldLeft((List[String](), new StringBuilder)) {
      case ((agg, sb), c) =>
        if (delimiters(c)) (c.toString :: sb.toString :: agg, new StringBuilder) // delimiter
        else (agg, sb append c)
    }.mapTo(e => e._2.toString :: e._1) // append last SB to list
      .filterNot(_.isEmpty)
      .reverse

  private def asciiNormalize(s: String): String =
    Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")

  def apply(str: String): String = {
    val split = splitWithDelimiters(str.trim)
    fixWord(split.head, isFirstWord = true) :: (split.tail map (fixWord(_, isFirstWord = false))) map asciiNormalize mkString ""
  }
}
