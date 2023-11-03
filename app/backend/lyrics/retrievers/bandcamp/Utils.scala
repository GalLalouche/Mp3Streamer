package backend.lyrics.retrievers.bandcamp

import java.util.regex.Pattern

import backend.lyrics.retrievers.{HtmlLyricsUtils, LyricParseResult}
import backend.Url
import org.jsoup.nodes.Element

import common.rich.primitives.RichString._
import common.rich.RichT._

private object Utils {
  val Source = "Bandcamp"

  def doesUrlMatchHost: Url => Boolean = _.address.matches(UrlPattern)
  private val UrlPattern = Pattern.compile(""".+\.bandcamp\.com/track/.*""")

  def elementToLyrics(e: Element): LyricParseResult.Lyrics =
    e.wholeText().trim() |> HtmlLyricsUtils.addBreakLines |> LyricParseResult.Lyrics
}
