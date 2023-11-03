package backend.lyrics.retrievers

import java.util.regex.Pattern

import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichString._
import common.rich.RichT._

private object HtmlLyricsUtils {
  private val NewLine = Pattern.compile(" *\r?\n *")
  private val CanonicalBreakLine = "<br>\n"
  private def ensureSuffix(s: String): String =
    s.mapIf(_.endsWith(CanonicalBreakLine).isFalse).to(_ + CanonicalBreakLine)
  def addBreakLines(s: String): String = s.replaceAll(NewLine, CanonicalBreakLine) |> ensureSuffix

  private val PrefixBr = Pattern.compile("\r?\n<br> *")
  def canonize(s: String): String = s
    .replaceAll(PrefixBr, CanonicalBreakLine)
    // Removes empty lines caused by moving <br>s up, e.g.,
    // foo
    // <br>
    // <br>
    // Will be changed to
    // foo<br>
    // <br>
    // <!-- empty line -->
    .split("\n")
    .view
    .filter(_.nonEmpty)
    .mkString("\n") |> ensureSuffix

  private val BreakLines = "((\r?\n)|(<br>))*"
  private val InitialBreakLinesPattern = Pattern.compile("^" + BreakLines)
  private val EndingBreakLinesPattern = Pattern.compile(BreakLines + "$")
  def trimBreakLines(s: String): String =
    s.removeAll(InitialBreakLinesPattern).removeAll(EndingBreakLinesPattern)
}
