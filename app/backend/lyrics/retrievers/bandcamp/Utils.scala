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

  def elementToLyrics(e: Element): LyricParseResult.Lyrics =
    e.wholeText().trim() |> HtmlLyricsUtils.addBreakLines |> LyricParseResult.Lyrics
}
