package mains

import java.util.Scanner
import common.rich.primitives.RichString._
private[mains] object StringFixer {
	private val lowerCaseWordsList = List("a", "ain't", "all", "am", "an", "and", "are", "as", "at", "be",
		"but", "by", "can", "can't", "cannot", "do", "don't", "for", "from", "get", "got", "gotten", "had",
		"has", "have", "her", "his", "in", "into", "is", "it", "it's", "its", "me", "mine", "my", "not",
		"of", "on", "or", "our", "ours", "theirs", "so", "should", "that", "the", "their", "them", "these",
		"this", "through", "those", "did", "to", "too", "up", "was", "were", "will", "with", "without", "won't",
		"would", "wouldn't", "your", "upon", "shall", "may", "there", "ov")
	private val lowerCaseWords = lowerCaseWordsList.toSet
	private val delimiters = """[ ()-:/\\]"""
	if (lowerCaseWords.toList.sorted != lowerCaseWordsList.sorted) // finds repeats
		println(lowerCaseWords.toList.sorted.map(""""%s"""".format(_)))

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
}