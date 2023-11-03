package common

import java.text.Normalizer
import java.util.regex.Pattern

import common.rich.primitives.RichString._

object LanguageString {
  private val HebrewPattern = """\p{InHebrew}""".r.unanchored
  private val NotAsciiPattern = Pattern.compile("""[^\p{ASCII}]""")

  implicit class languageString($ : String) {
    def hasHebrew: Boolean = HebrewPattern.findFirstIn($).isDefined

    def keepAscii: String = Normalizer.normalize($, Normalizer.Form.NFD).removeAll(NotAsciiPattern)
  }
}
