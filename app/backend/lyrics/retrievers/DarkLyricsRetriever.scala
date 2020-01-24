package backend.lyrics.retrievers

import java.util.regex.Pattern

import com.google.common.annotations.VisibleForTesting
import javax.inject.Inject
import models.Song
import org.jsoup.nodes.Document

import common.rich.RichT._
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichString._

private class DarkLyricsRetriever @Inject()(singleHostHelper: SingleHostParsingHelper)
    extends HtmlRetriever {
  import DarkLyricsRetriever._

  override val parse = singleHostHelper(parser)

  private val urlHelper = new SingleHostUrlHelper(url, parse)
  override val get = urlHelper.get
  override val doesUrlMatchHost = urlHelper.doesUrlMatchHost
}

private object DarkLyricsRetriever {
  @VisibleForTesting
  private[retrievers] val url: SingleHostUrl = new SingleHostUrl {
    private def normalize(s: String): String = s.toLowerCase.filter(_.isLetter)

    override val hostPrefix: String = "http://www.darklyrics.com/lyrics"
    override def urlFor(s: Song): String =
      s"$hostPrefix/${normalize(s.artistName)}/${normalize(s.albumName)}.html#${s.track}"
  }

  @VisibleForTesting
  private[retrievers] val parser: SingleHostParser = new SingleHostParser {
    private val BreakLinesOrNewLinePattern = Pattern compile "((<br>)|\\n)"
    private def isInstrumental(html: String) =
      html.removeAll(BreakLinesOrNewLinePattern).toLowerCase == "<i>[instrumental]</i>"
    private val BreakLinesPattern = Pattern compile "<br>"
    private def removeEndingBreakLines(ss: Seq[String]) =
      ss.reverse.dropWhile(_.matches(BreakLinesPattern)).reverse

    override val source: String = "DarkLyrics"
    // HTML is structured for shit, so might as well parse it by hand
    override def apply(html: Document, s: Song) = {
      val $ = html.toString
          .split("\n").toList
          .dropWhile(_.matches(s""".*a name="${s.track}".*""").isFalse)
          .drop(1)
          .takeWhile(e => e.matches(".*<h3>.*").isFalse && e.matches(".*<div.*").isFalse) // this fucking site...
          .map(_.trim)
          .mapTo(removeEndingBreakLines)
          .mkString("\n")
      if (isInstrumental($)) LyricParseResult.Instrumental else LyricParseResult.Lyrics($)
    }
  }
}
