package mains.fixer

import java.text.Normalizer

import com.google.common.annotations.VisibleForTesting
import common.rich.RichT._
import common.rich.primitives.RichBoolean._

object StringFixer {
  @VisibleForTesting
  private[fixer] val lowerCaseWords = List("a", "ain't", "all", "am", "an", "and", "are", "aren't", "as", "at", "be", "but", "by", "can", "can't",
    "cannot", "did", "didn't", "do", "doesn't", "don't", "for", "from", "get", "got", "gotten", "had", "has", "have", "her", "his", "in", "into", "is",
    "isn't", "it", "it's", "its", "may", "me", "mine", "my", "not", "of", "on", "or", "our", "ours", "ov", "shall", "should", "so", "than",
    "that", "the", "their", "theirs", "them", "then", "there", "these", "this", "those", "through", "to", "too", "up", "upon", "was", "wasn't",
    "were", "weren't", "will", "with", "without", "won't", "would", "wouldn't", "your")
  private val lowerCaseSet = lowerCaseWords.toSet
  private val delimiters = " ()-:/\\".toSet

  private def pascalCaseWord(w: String): String = w.toLowerCase.capitalize

  private def fixWord(word: String, isFirstWord: Boolean): String =
    if (isFirstWord.isFalse && lowerCaseSet(word.toLowerCase)) word.toLowerCase
    else if (word.matches(".*[A-Z].*")) word // mixed caps
    else if (word.head.isDigit) word.toLowerCase // 1st, 2nd, etc.
    else if (word matches "[IVXMLivxml]+") word.toUpperCase // roman numbers, also handles pronoun "I"
    else if (word matches "(\\w\\.)+") word.toUpperCase // A.B.C. pattern
    else pascalCaseWord(word)

  private def splitWithDelimiters(s: String): List[String] =
    s.foldLeft((List[String](), new StringBuilder)) {
      case ((agg, sb), c) =>
        if (delimiters(c)) (c.toString :: sb.toString :: agg, new StringBuilder) // delimiter
        else (agg, sb append c)
    }.mapTo(e => e._2.toString :: e._1) // append last SB to list
        .filterNot(_.isEmpty)
        .reverse

  // Modified from https://stackoverflow.com/a/29364083/736508
  private val cyrillicMap = Map(
    ' ' -> " ", 'A' -> "A", 'B' -> "B", 'C' -> "C", 'D' -> "D", 'E' -> "E", 'F' -> "F", 'G' -> "G",
    'H' -> "H", 'I' -> "I", 'J' -> "J", 'K' -> "K", 'L' -> "L", 'M' -> "M", 'N' -> "N", 'O' -> "O",
    'P' -> "P", 'Q' -> "Q", 'R' -> "R", 'S' -> "S", 'T' -> "T", 'U' -> "U", 'V' -> "V", 'W' -> "W",
    'X' -> "X", 'Y' -> "Y", 'Z' -> "Z", 'a' -> "a", 'b' -> "b", 'c' -> "c", 'd' -> "d", 'e' -> "e",
    'f' -> "f", 'g' -> "g", 'h' -> "h", 'i' -> "i", 'j' -> "j", 'k' -> "k", 'l' -> "l", 'm' -> "m",
    'n' -> "n", 'o' -> "o", 'p' -> "p", 'q' -> "q", 'r' -> "r", 's' -> "s", 't' -> "t", 'u' -> "u",
    'v' -> "v", 'w' -> "w", 'x' -> "x", 'y' -> "y", 'z' -> "z", 'Ё' -> "E", 'А' -> "A", 'Б' -> "B",
    'В' -> "V", 'Г' -> "G", 'Д' -> "D", 'Е' -> "E", 'Ж' -> "Zh", 'З' -> "Z", 'И' -> "I", 'Й' -> "Y",
    'К' -> "K", 'Л' -> "L", 'М' -> "M", 'Н' -> "N", 'О' -> "O", 'П' -> "P", 'Р' -> "R", 'С' -> "S",
    'Т' -> "T", 'У' -> "U", 'Ф' -> "F", 'Х' -> "H", 'Ц' -> "Ts", 'Ч' -> "Ch", 'Ш' -> "Sh",
    'Щ' -> "Sch", 'Ъ' -> "", 'Ы' -> "I", 'Ь' -> "", 'Э' -> "E", 'Ю' -> "Ju", 'Я' -> "Ja",
    'а' -> "a", 'б' -> "b", 'в' -> "v", 'г' -> "g", 'д' -> "d", 'е' -> "e", 'ж' -> "zh", 'з' -> "z",
    'и' -> "i", 'й' -> "y", 'к' -> "k", 'л' -> "l", 'м' -> "m", 'н' -> "n", 'о' -> "o", 'п' -> "p",
    'р' -> "r", 'с' -> "s", 'т' -> "t", 'у' -> "u", 'ф' -> "f", 'х' -> "kh", 'ц' -> "ts",
    'ч' -> "ch", 'ш' -> "sh", 'щ' -> "sch", 'ъ' -> "", 'ы' -> "i", 'ь' -> "", 'э' -> "e",
    'ю' -> "ju", 'я' -> "ja", 'ё' -> "e", 'і' -> "i", '’' -> "'")
  private def asciiNormalize(s: String): String = {
    import common.rich.primitives.RichString._
    if (s.isWhitespaceOrEmpty) s
    else {
      val withoutSpecialQuotes = s.replaceAll("[‘’“”]", "'")
      Normalizer.normalize(withoutSpecialQuotes, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
          .mapIf(_.isWhitespaceOrEmpty).to(asciiNormalize(withoutSpecialQuotes.map(cyrillicMap).mkString("")))
    }
  }

  def apply(str: String): String = {
    val head :: tail = splitWithDelimiters(str.trim)
    val fixed = fixWord(head, isFirstWord = true) :: tail.map(fixWord(_, isFirstWord = false))
    fixed map asciiNormalize mkString ""
  }
}
