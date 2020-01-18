package backend.lyrics.retrievers

import java.util.regex.Pattern

import common.rich.primitives.RichString._

private object HtmlLyricsUtils {
  private val NewLine = Pattern compile "\r?\n"
  def addBreakLines(s: String): String = s.replaceAll(NewLine, "<br>\n")

  private val PrefixBr = Pattern compile "\r?\n<br> *"
  def canonize(s: String): String = s.replaceAll(PrefixBr, "<br>\n")

  private val BreakLines = "((\r?\n)|(<br>))*"
  private val InitialBreakLinesPattern = Pattern compile "^" + BreakLines
  private val EndingBreakLinesPattern = Pattern compile BreakLines + "$"
  def trimBreakLines(s: String): String =
    s.removeAll(InitialBreakLinesPattern).removeAll(EndingBreakLinesPattern)
}
