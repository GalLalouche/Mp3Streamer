package backend.lyrics.retrievers.genius

import java.util.regex.Pattern

import backend.lyrics.retrievers.{LyricParseResult, SingleHostParser}
import models.Song
import org.jsoup.nodes.Document

import common.RichJsoup._
import common.rich.primitives.RichString._

private object LyricsParser extends SingleHostParser {
  private val Annotations = Pattern compile """\[.*?\]"""
  private val GapBetweenAnnotations = "\n\n\n"
  override val source = "GeniusLyrics"
  override def apply(d: Document, s: Song): LyricParseResult =
    d.selectSingle("div .lyrics").wholeText.trim match {
      case "[Instrumental]" | "Instrumental" => LyricParseResult.Instrumental
      case s => LyricParseResult.Lyrics(
        s.removeAll(Annotations)
            .simpleReplace(GapBetweenAnnotations, "\n\n")
            .trim
            .simpleReplace("\n", "\n<BR>\n")
      )
    }
}
