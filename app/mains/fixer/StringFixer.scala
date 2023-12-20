package mains.fixer

import java.util.regex.Pattern
import javax.inject.Inject
import scala.io.Source

import backend.logging.{ConsoleLogger, Logger}
import com.google.common.annotations.VisibleForTesting
import common.rich.collections.RichSeq._
import common.rich.collections.RichTraversableOnce._
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichString._
import common.rich.RichT._
import common.LanguageString._
import org.apache.commons.lang3.StringUtils
import resource._

class StringFixer @Inject() (logger: Logger) extends (String => String) {
  import StringFixer._

  def asciiNormalize(s: String): String = try {
    if (s.isWhitespaceOrEmpty)
      return s
    val withoutSpecialCharacters =
      s.replaceAll(SpecialQuotes, "'") |> normalizeDashesAndApostrophes |> StringUtils.stripAccents
    withoutSpecialCharacters.keepAscii
      .mapIf(_.length < withoutSpecialCharacters.length)
      .to(asciiNormalize(withoutSpecialCharacters.flatMap(toAscii.apply)))
  } catch {
    case e: Exception =>
      // TODO reuse this for Hebrew check as well?
      val lang = DetectLanguage(s)
      if (isExemptLanguage(lang)) {
        logger.verbose(s"Could not asciify <$s>")
        s
      } else
        throw e
  }
  // TODO reuse this for Hebrew check as well?
  private def isExemptLanguage(lang: String) =
    // Japanese and Chinese. Life is too short to start asciing those.
    lang == "ja" || lang.startsWith("ch") || lang.startsWith("zh")

  override def apply(s: String): String = {
    val trimmed = s.trim
    if (trimmed.hasHebrew)
      trimmed
        .replaceAll(
          SpecialQuotes,
          "\"",
        ) |> normalizeDashesAndApostrophes // Keep '"' for Hebrew acronyms.
    else {
      val words = trimmed.splitWithDelimiters(Delimiters)
      // The first word is always capitalized (e.g., The Who), while the other words will only be
      // force-capitalized if they appear after a separator, e.g., The Whole (The Song of the Band).
      fixWord(words.head, forceCapitalization = true) + words.pairSliding
        .map { case (wordBefore, word) =>
          fixWord(word, forceCapitalization = wordBefore.trim.matches(Delimiters))
        }
        .mkString("")
        .replaceAll(ConjuctiveN, " n'")
        .replaceAll(Vs, " vs. ")
    }
  }

  private def fixWord(unnormalizedWord: String, forceCapitalization: Boolean): String = {
    val word = asciiNormalize(unnormalizedWord)
    if (forceCapitalization.isFalse && lowerCaseSet(word.toLowerCase)) word.toLowerCase
    else if (word.matches(MixedCapsPattern)) word // mixed caps
    else if (word.head.isDigit) word.toLowerCase // 1st, 2nd, etc.
    else if (word.matches(RomanPattern)) word.toUpperCase // roman numbers, also handles pronoun "I"
    else if (word.matches(DottedAcronymPattern)) word.toUpperCase // A.B.C. pattern
    else pascalCaseWord(word)
  }
}

object StringFixer extends StringFixer(ConsoleLogger) {
  @VisibleForTesting
  private[fixer] val lowerCaseWords = Vector(
    "'em",
    "a",
    "ain't",
    "all",
    "am",
    "an",
    "and",
    "are",
    "aren't",
    "as",
    "at",
    "be",
    "but",
    "by",
    "can",
    "can't",
    "cannot",
    "de",
    "did",
    "didn't",
    "do",
    "doesn't",
    "don't",
    "for",
    "from",
    "get",
    "got",
    "gotten",
    "had",
    "has",
    "have",
    "her",
    "his",
    "in",
    "into",
    "is",
    "isn't",
    "it",
    "it's",
    "its",
    "may",
    "me",
    "mine",
    "my",
    "not",
    "of",
    "on",
    "or",
    "our",
    "ours",
    "ov",
    "shall",
    "should",
    "so",
    "than",
    "that",
    "the",
    "their",
    "theirs",
    "them",
    "then",
    "there",
    "these",
    "thine",
    "this",
    "those",
    "through",
    "thy",
    "to",
    "too",
    "up",
    "upon",
    "van",
    "von",
    "was",
    "wasn't",
    "were",
    "weren't",
    "will",
    "with",
    "without",
    "won't",
    "would",
    "wouldn't",
    "your",
  )
  private val lowerCaseSet = lowerCaseWords.toSet

  private def pascalCaseWord(w: String): String = w.toLowerCase.capitalize

  private val RomanPattern = Pattern.compile("[IVXMLivxml]+")
  private val MixedCapsPattern = Pattern.compile(".*[A-Z].*")
  private val DottedAcronymPattern = Pattern.compile("(\\w\\.)+")
  private val ConjuctiveN: Pattern = Pattern.compile(" '?[Nn]'")
  private val Vs: Pattern = Pattern.compile(""" vs\.? """, Pattern.CASE_INSENSITIVE)
  val SpecialQuotes: Pattern = Pattern.compile("[“”]")
  val SpecialApostrophes: Pattern = Pattern.compile("[‘’�´]")
  private val SpecialDashes = Pattern.compile("[—–-−‐]")

  private val Delimiters = Pattern.compile("""[ ()\-:/"&]+""")

  // Modified from https://stackoverflow.com/a/29364083/736508
  // TODO RichFile should really start using UTF-8 by default
  private val toAscii: Map[Char, String] =
    managed(Source.fromInputStream(getClass.getResourceAsStream("ascii.txt"), "UTF-8"))
      .map(_.getLines().map(_.splitParse(":", _.toSeq.single, identity)).toMap)
      .tried
      .get
      .++(33.to(126).map(_.toChar :-> (_.toString)))
  private def normalizeDashesAndApostrophes(s: String) =
    s.replaceAll(SpecialApostrophes, "'").replaceAll(SpecialDashes, "-")
}
