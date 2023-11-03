package backend.lyrics.retrievers.genius

import java.util.regex.Pattern
import scala.collection.JavaConverters._

import backend.lyrics.retrievers.{HtmlLyricsUtils, LyricParseResult, SingleHostParser}
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.rich.primitives.RichString._
import common.rich.RichT.richT
import common.RichJsoup._
import models.Song
import org.jsoup.nodes.{Document, Element, TextNode}

private object LyricsParser extends SingleHostParser {
  private val Annotations = Pattern.compile("""\[.*?\]""")
  private val GapBetweenAnnotations = Pattern.compile("\n{2,}")
  private val EmptyLeadingLines = Pattern.compile("^\n*")
  // Because some assholes add (Chorus 1) to the lyrics instead of the annotations.
  private val SongParts = Pattern.compile("((^)|(\n{1,2}))\\(\\w+( \\d+)?\\)\n")
  override val source = "GeniusLyrics"
  override def apply(d: Document, s: Song): LyricParseResult = {
    val v = d.selectIterator("#lyrics-root div[data-lyrics-container]").toVector
    if (v.isEmpty)
      if (d.wholeText.contains("This song is an instrumental"))
        LyricParseResult.Instrumental
      else
        throw new IllegalArgumentException("Unexpected HTML structure")
    else if (v == Vector("[Instrumental]") || v == Vector("Instrumental"))
      LyricParseResult.Instrumental
    else
      LyricParseResult.Lyrics(
        v.flatMap(go)
          .mkString("")
          .removeAll(Annotations)
          .replaceAll(GapBetweenAnnotations, "\n\n")
          .replaceAll(SongParts, "\n\n")
          .removeAll(EmptyLeadingLines)
          |> HtmlLyricsUtils.addBreakLines,
      )
  }

  private val LineBreaks = Set("br", "div")
  private def go(el: Element): Seq[String] =
    el.childNodes.asScala.toStream
      .flatMap {
        case e: Element =>
          if (LineBreaks contains e.tagName)
            Vector("\n")
          else if (e.tagName == "a")
            e.childNodes.asScala.single
              .asInstanceOf[Element]
              .ensuring(_.tagName == "span")
              .childNodes
              .asScala
              .map {
                case e: TextNode => e.getWholeText
                case e: Element if e.tagName == "br" => "\n"
              }
          else if (e.tagName == "i")
            go(e)
          else
            Vector.empty
        case e: TextNode => Vector(e.getWholeText)
      }
}
