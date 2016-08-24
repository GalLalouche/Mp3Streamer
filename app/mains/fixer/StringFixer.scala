package mains.fixer

import common.rich.RichT._

private object StringFixer {

  private val lowerCaseWords = Set("a", "ain't", "all", "am", "an", "and", "are", "aren't", "as", "at", "be", "but", "by", "can", "can't",
    "cannot", "did", "didn't", "do", "don't", "for", "from", "get", "got", "gotten", "had", "has", "have", "her", "his", "in", "into", "is",
    "isn't", "it", "it's", "its", "may", "me", "mine", "my", "not", "of", "on", "or", "our", "ours", "ov", "shall", "should", "so", "than",
    "that", "the", "their", "theirs", "them", "then", "there", "these", "this", "those", "through", "to", "too", "up", "upon", "was", "wasn't",
    "were", "weren't", "will", "with", "without", "won't", "would", "wouldn't", "your")
  private val delimiters = """[ ()-:/\\]"""

  private def pascalCaseWord(w: String): String = w.head.toUpper + w.tail.toLowerCase

  private def fixWord(word: String): String = word match {
    case e if e matches delimiters => e
    case e if e.head.isDigit => e.toLowerCase // 1st, 2nd, etc.
    case "a" | "A" => "a"
    case _ if word.matches("[A-Z]+") => word
    case "i" | "I" => "I"
    case s if s matches "[IVXMLivxml]+" => s toUpperCase // roman numbers
    case _ => if (lowerCaseWords(word.toLowerCase)) word.toLowerCase else pascalCaseWord(word) // everything else
  }

  private def splitWithDelimiters($: String, pattern: String): Seq[String] =
    $.foldLeft((List[String](), new StringBuilder)) {
      case ((agg, sb), c) =>
        if (c.toString.matches(pattern)) (c.toString :: sb.toString :: agg, new StringBuilder) // delimiter
        else (agg, sb append c)
    }.mapTo(e => e._2.toString :: e._1) // append last SB to list
      .filterNot(_.isEmpty) // remove empty ""
      .reverse
  def apply(str: String): String = {
    val split = splitWithDelimiters(str, delimiters).toList
    (pascalCaseWord(split.head) :: (split.tail map fixWord)) mkString ""
  }

  def main(args: Array[String]) {
    println("List(" + lowerCaseWords.toList.sorted.map(e => "\"" + e + "\"").mkString(", ") + ")")
  }
}
