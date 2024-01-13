package backend.lyrics.retrievers.bandcamp

import java.util.regex.Pattern

import backend.lyrics.retrievers.{HtmlLyricsUtils, LyricParseResult}
import io.lemonlabs.uri.Url
import org.jsoup.nodes.Element

import common.rich.RichT._
import common.rich.primitives.RichString._

private object Utils {
  val Source = "Bandcamp"

  def doesUrlMatchHost: Url => Boolean = _.toStringPunycode.matches(UrlPattern)
  private val UrlPattern = Pattern.compile(""".+\.bandcamp\.com/track/.*""")

  def elementToLyrics(e: Element): LyricParseResult =
    e.wholeText().trim |> HtmlLyricsUtils.addBreakLines |> parse

  private def parse(s: String): LyricParseResult =
    if (HtmlLyricsUtils.trimBreakLines(s).matches(InstrumentalPattern))
      LyricParseResult.Instrumental
    else
      LyricParseResult.Lyrics(s)

  private val InstrumentalPattern: Pattern =
    Pattern.compile("""\[?instrumental]""", Pattern.CASE_INSENSITIVE)
}
