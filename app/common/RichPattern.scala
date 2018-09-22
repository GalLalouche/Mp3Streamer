package common

import java.util.regex.Pattern
import common.rich.primitives.RichBoolean._

// TODO move to common
object RichPattern {
  implicit class richPattern(private val $: Pattern) extends AnyVal {
    // TODO move both to richString
    def matches(s: String): Boolean = $.matcher(s).matches()
    def doesNotMatch(s: String): Boolean = matches(s).isFalse
  }
}
