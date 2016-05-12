package mains.fixer

import common.rich.primitives.RichString._

private object StringFixer {

  private val lowerCaseWords = Set("a", "ain't", "all", "am", "an", "and", "are", "aren't", "as", "at", "be", "but", "by", "can", "can't",
    "cannot", "did", "didn't", "do", "don't", "for", "from", "get", "got", "gotten", "had", "has", "have", "her", "his", "in", "into", "is",
    "isn't", "it", "it's", "its", "may", "me", "mine", "my", "not", "of", "on", "or", "our", "ours", "ov", "shall", "should", "so", "than",
    "that", "the", "their", "theirs", "them", "then", "there", "these", "this", "those", "through", "to", "too", "up", "upon", "was", "wasn't",
    "were", "weren't", "will", "with", "without", "won't", "would", "wouldn't", "your")
  private val delimiters = """[ ()-:/\\]"""

  private def pascalCaseWord(w: String): String = w.head.toUpper + w.tail.toLowerCase

  private def fixWord(w: String): String = w match {
    case e if e matches delimiters => e
    case "a" | "A" => "a"
    case _ if w.matches("[A-Z]+") => w
    case "i" | "I" => "I"
    case s if s matches "[IVXMLivxml]+" => s toUpperCase // roman numbers
    case _ => if (lowerCaseWords(w.toLowerCase)) w.toLowerCase else pascalCaseWord(w) // everything else
  }

  def apply(str: String): String = {
    val split = str.splitWithDelimiters(delimiters).toList
    (pascalCaseWord(split.head) :: (split.tail map fixWord)) mkString ""
  }

  def main(args: Array[String]) {
    println("List(" + lowerCaseWords.toList.sorted.map(e => "\"" + e + "\"").mkString(", ") + ")")
  }
}
