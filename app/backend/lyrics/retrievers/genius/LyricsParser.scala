package backend.lyrics.retrievers.genius

import java.util.regex.Pattern

import backend.lyrics.retrievers.{HtmlLyricsUtils, LyricParseResult, SingleHostParser}
import models.Song
import org.jsoup.nodes.Document

import common.rich.RichT._
import common.RichJsoup._
import common.rich.primitives.RichString._

private object LyricsParser extends SingleHostParser {
  private val Annotations = Pattern compile """\[.*?\]"""
  private val GapBetweenAnnotations = Pattern compile "\n{2,}"
  override val source = "GeniusLyrics"
  override def apply(d: Document, s: Song): LyricParseResult =
    d.selectSingle("div .lyrics").wholeText.trim match {
      case "[Instrumental]" | "Instrumental" => LyricParseResult.Instrumental
      case s => LyricParseResult.Lyrics(
        s.removeAll(Annotations)
            .replaceAll(GapBetweenAnnotations, "\n\n")
            .trim |> HtmlLyricsUtils.addBreakLines
      )
    }
}
