package mains.fixer

import java.util.regex.Pattern

import com.google.common.annotations.VisibleForTesting

import common.rich.collections.RichSeq._
import common.rich.RichT._
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichString._
import common.LanguageString._

object StringFixer extends (String => String) {
  @VisibleForTesting
  private[fixer] val lowerCaseWords = List("a", "ain't", "all", "am", "an", "and", "are", "aren't", "as",
    "at", "be", "but", "by", "can", "can't", "cannot", "did", "didn't", "do", "doesn't", "don't", "for",
    "from", "get", "got", "gotten", "had", "has", "have", "her", "his", "in", "into", "is", "isn't", "it",
    "it's", "its", "may", "me", "mine", "my", "not", "of", "on", "or", "our", "ours", "ov", "shall", "should",
    "so", "than", "that", "the", "their", "theirs", "them", "then", "there", "these", "this", "those",
    "through", "to", "too", "up", "upon", "van", "von", "was", "wasn't", "were", "weren't", "will", "with",
    "without", "won't", "would", "wouldn't", "your")
  private val lowerCaseSet = lowerCaseWords.toSet

  private def pascalCaseWord(w: String): String = w.toLowerCase.capitalize

  private val RomanPattern = Pattern compile "[IVXMLivxml]+"
  private val MixedCapsPattern = Pattern compile ".*[A-Z].*"
  private val DottedAcronymPattern = Pattern compile "(\\w\\.)+"
  private val SpecialQuotes = Pattern compile "[“”]"
  private val SpecialApostrophes = Pattern compile "[‘’]"
  private val SpecialDashes = Pattern compile "[—–-−]"
  private def fixWord(word: String, forceCapitalization: Boolean): String = asciiNormalize(
    if (forceCapitalization.isFalse && lowerCaseSet(word.toLowerCase)) word.toLowerCase
    else if (word matches MixedCapsPattern) word // mixed caps
    else if (word.head.isDigit) word.toLowerCase // 1st, 2nd, etc.
    else if (word matches RomanPattern) word.toUpperCase // roman numbers, also handles pronoun "I"
    else if (word matches DottedAcronymPattern) word.toUpperCase // A.B.C. pattern
    else pascalCaseWord(word)
  )

  private val Delimiters = Pattern compile """[ ()\-:/"&]+"""

  // Modified from https://stackoverflow.com/a/29364083/736508
  private val toAscii = Map(
    ' ' -> " ", 'A' -> "A", 'Æ' -> "Ae", 'B' -> "B", 'C' -> "C", 'D' -> "D", 'E' -> "E", 'F' -> "F",
    'G' -> "G", 'H' -> "H", 'I' -> "I", 'J' -> "J", 'K' -> "K", 'L' -> "L", 'M' -> "M", 'N' -> "N",
    'O' -> "O", 'P' -> "P", 'Q' -> "Q", 'R' -> "R", 'S' -> "S", 'T' -> "T", 'U' -> "U", 'V' -> "V",
    'W' -> "W", 'X' -> "X", 'Y' -> "Y", 'Z' -> "Z",
    'a' -> "a", 'æ' -> "ae", 'b' -> "b", 'c' -> "c", 'd' -> "d", 'e' -> "e", 'f' -> "f", 'g' -> "g",
    'h' -> "h", 'i' -> "i", 'j' -> "j", 'k' -> "k", 'l' -> "l", 'm' -> "m", 'n' -> "n", 'o' -> "o",
    'ø' -> "o", 'p' -> "p", 'q' -> "q", 'r' -> "r", 's' -> "s", 't' -> "t", 'u' -> "u", 'v' -> "v",
    'w' -> "w", 'x' -> "x", 'y' -> "y", 'z' -> "z",
    'Ё' -> "E", 'А' -> "A", 'Б' -> "B", 'В' -> "V", 'Г' -> "G", 'Д' -> "D", 'Е' -> "E", 'Ж' -> "Zh",
    'З' -> "Z", 'И' -> "I", 'Й' -> "Y", 'К' -> "K", 'Л' -> "L", 'М' -> "M", 'Н' -> "N", 'О' -> "O",
    'П' -> "P", 'Р' -> "R", 'С' -> "S", 'Т' -> "T", 'У' -> "U", 'Ф' -> "F", 'Х' -> "H", 'Ц' -> "Ts",
    'Ч' -> "Ch", 'Ш' -> "Sh", 'Щ' -> "Sch", 'Ъ' -> "", 'Ы' -> "I", 'Ь' -> "", 'Э' -> "E", 'Ю' -> "Ju",
    'Я' -> "Ja", 'а' -> "a", 'б' -> "b", 'в' -> "v", 'г' -> "g", 'д' -> "d", 'е' -> "e", 'ж' -> "zh",
    'з' -> "z", 'и' -> "i", 'й' -> "y", 'к' -> "k", 'л' -> "l", 'м' -> "m", 'н' -> "n", 'о' -> "o",
    'п' -> "p", 'р' -> "r", 'с' -> "s", 'т' -> "t", 'у' -> "u", 'ф' -> "f", 'х' -> "kh", 'ц' -> "ts",
    'ч' -> "ch", 'ш' -> "sh", 'щ' -> "sch", 'ъ' -> "", 'ы' -> "i", 'ь' -> "", 'э' -> "e",
    'ю' -> "ju", 'я' -> "ja", 'ё' -> "e", 'і' -> "i", '’' -> "'")
  private def normalizeDashesAndApostrophes(s: String) =
    s.replaceAll(SpecialApostrophes, "'").replaceAll(SpecialDashes, "-")
  private def asciiNormalize(s: String): String = {
    import common.rich.primitives.RichString._
    if (s.isWhitespaceOrEmpty)
      return s
    val withoutSpecialCharacters =
      s.replaceAll(SpecialQuotes, "'") |> normalizeDashesAndApostrophes
    withoutSpecialCharacters.keepAscii
        .mapIf(_.length < withoutSpecialCharacters.length)
        .to(asciiNormalize(withoutSpecialCharacters.flatMap(toAscii(_))))
  }

  override def apply(s: String): String = {
    val trimmed = s.trim
    if (trimmed.hasHebrew)
    // Keep '"' for Hebrew acronyms.
      trimmed.replaceAll(SpecialQuotes, "\"") |> normalizeDashesAndApostrophes
    else {
      val words = trimmed.splitWithDelimiters(Delimiters)
      // The first word is always capitalized (e.g., The Who), while the other words will only be
      // force-capitalized if they appear after a separator, e.g., The Whole (The Song of the Band).
      fixWord(words.head, forceCapitalization = true) + words.pairSliding.map {
        case (wordBefore, word) => fixWord(word, forceCapitalization = wordBefore.trim.matches(Delimiters))
      }.mkString("")
    }
  }
}
