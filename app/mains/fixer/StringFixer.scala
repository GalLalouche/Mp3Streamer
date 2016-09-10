package mains.fixer

import java.text.Normalizer

import common.rich.RichT._

private object StringFixer {
  private[fixer] val lowerCaseWords = List("a", "ain't", "all", "am", "an", "and", "are", "aren't", "as", "at", "be", "but", "by", "can", "can't",
    "cannot", "did", "didn't", "do", "doesn't", "don't", "for", "from", "get", "got", "gotten", "had", "has", "have", "her", "his", "in", "into", "is",
    "isn't", "it", "it's", "its", "may", "me", "mine", "my", "not", "of", "on", "or", "our", "ours", "ov", "shall", "should", "so", "than",
    "that", "the", "their", "theirs", "them", "then", "there", "these", "this", "those", "through", "to", "too", "up", "upon", "was", "wasn't",
    "were", "weren't", "will", "with", "without", "won't", "would", "wouldn't", "your")
  private val lowerCaseSet = lowerCaseWords.toSet
  private val delimiters = """[ ()\-:/\\]"""

  private def pascalCaseWord(w: String): String = w.head.toUpper + w.tail.toLowerCase

  private def fixWord(word: String): String = word match {
    case e if e matches delimiters => e
    case e if e.head.isDigit => e.toLowerCase // 1st, 2nd, etc.
    case "a" | "A" => "a"
    case _ if word.matches("[A-Z]+") => word // all caps
    case "i" | "I" => "I"
    case s if s matches "[IVXMLivxml]+" => s.toUpperCase // roman numbers
    case s if s matches "\\d\\w{2}" => s.toLowerCase // 1st, 2nd, etc.
    case _ => if (word.toLowerCase |> lowerCaseSet) word.toLowerCase else word |> pascalCaseWord
  }

  private def splitWithDelimiters(s: String): List[String] =
    s.foldLeft((List[String](), new StringBuilder)) {
      case ((agg, sb), c) =>
        if (c.toString matches delimiters) (c.toString :: sb.toString :: agg, new StringBuilder) // delimiter
        else (agg, sb append c)
    }.mapTo(e => e._2.toString :: e._1) // append last SB to list
      .filterNot(_.isEmpty)
      .reverse
  private def asciiNormalize(s: String): String =
    Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
  def apply(str: String): String = {
    val split = splitWithDelimiters(str)
    (pascalCaseWord(split.head) :: (split.tail map fixWord)) map asciiNormalize mkString ""
  }
}
