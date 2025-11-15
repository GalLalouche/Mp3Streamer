package backend.lyrics.retrievers

import java.util.regex.Pattern

import com.google.common.annotations.VisibleForTesting
import com.google.inject.Inject
import models.Song
import org.jsoup.nodes.Document

import common.rich.RichT._
import common.rich.primitives.RichString._

private class DarkLyricsRetriever @Inject() (singleHostHelper: SingleHostParsingHelper)
    extends AmphiRetriever {
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
      s"$hostPrefix/${normalize(s.artistName)}/${normalize(s.albumName)}.html#${s.trackNumber}"
  }

  @VisibleForTesting
  private[retrievers] val parser: SingleHostParser = new SingleHostParser {
    private val BreakLinesOrNewLinePattern = Pattern.compile("""((<br>)|\n)""")
    private def isInstrumental(html: String) =
      html.removeAll(BreakLinesOrNewLinePattern).equalsIgnoreCase("<i>[instrumental]</i>")

    override val source: String = "DarkLyrics"
    // HTML is structured for shit, so might as well parse it by hand.
    private val Heading = Pattern.compile(".*<h3>.*")
    private val Div = Pattern.compile(".*<div.*")
    override def apply(html: Document, s: Song) = {
      val currentTrack = Pattern.compile(s""".*a name="${s.trackNumber}".*""")
      val $ = html.toString
        .split("\n")
        .iterator
        .dropWhile(_.doesNotMatch(currentTrack))
        .drop(1)
        .takeWhile(e => e.doesNotMatch(Heading) && e.doesNotMatch(Div)) // this fucking site...
        .map(_.trim)
        .mkString("\n")
        .|>(HtmlLyricsUtils.trimBreakLines)
        .trim
        .|>(HtmlLyricsUtils.canonize)

      if (isInstrumental($)) LyricParseResult.Instrumental else LyricParseResult.Lyrics($)
    }
  }
}
