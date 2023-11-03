package common.io

import java.util.regex.Pattern

private object UtfDecoder {
  private val pattern = Pattern.compile("""\\u([0-9a-fA-F]{4})""")
  def apply(s: String): String = {
    val m = pattern.matcher(s)
    val $ = new StringBuffer()
    def decode(s: String): String = Integer.parseInt(s, 16).toChar.toString
    while (m.find())
      m.appendReplacement($, decode(m.group(1)))
    m.appendTail($)
    $.toString
  }
}
