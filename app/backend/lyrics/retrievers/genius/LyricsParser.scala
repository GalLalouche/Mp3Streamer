package backend.lyrics.retrievers.genius

import java.util.regex.Pattern

import backend.lyrics.retrievers.{LyricParseResult, SingleHostParser}
import models.Song
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

import common.rich.collections.RichTraversableOnce._
import common.rich.primitives.RichString._

private object LyricsParser extends SingleHostParser {
  private val Annotations = Pattern compile ("""\[.*?\]""")
  private val GapBetweenAnnotations = Pattern compile ("""\n{3}""")
  private val NewLines = Pattern compile "\n"
  override val source = "GeniusLyrics"
  override def apply(d: Document, s: Song): LyricParseResult =
    d.select("div .lyrics").asScala.single.wholeText.trim match {
      case "[Instrumental]" => LyricParseResult.Instrumental
      case s => LyricParseResult.Lyrics(
        s.removeAll(Annotations)
            .replaceAll(GapBetweenAnnotations, "\n\n")
            .trim
            .replaceAll(NewLines, "\n<BR>\n")
      )
    }
}
