package common

import java.text.Normalizer
import java.util.regex.Pattern

import common.rich.primitives.RichString._

object LanguageString {
  private val hebrewPattern = """\p{InHebrew}""".r.unanchored
  private val notAsciiPattern = Pattern.compile("[^\\p{ASCII}]")

  implicit class languageString($: String) {
    def hasHebrew: Boolean = hebrewPattern.findFirstIn($).isDefined

    def keepAscii: String = Normalizer.normalize($, Normalizer.Form.NFD).replaceAll(notAsciiPattern, "")
  }
}
