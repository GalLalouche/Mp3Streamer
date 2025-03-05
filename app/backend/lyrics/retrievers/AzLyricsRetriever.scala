package backend.lyrics.retrievers

import com.google.common.annotations.VisibleForTesting
import com.google.inject.Inject
import models.Song
import org.jsoup.nodes.Document

import common.RichJsoup._
import common.rich.RichT.richT
import common.rich.collections.RichTraversableOnce.richTraversableOnce

private class AzLyricsRetriever @Inject() (singleHostHelper: SingleHostParsingHelper)
    extends HtmlRetriever {
  import AzLyricsRetriever._

  override val parse = singleHostHelper(parser)
  private val urlHelper = new SingleHostUrlHelper(url, parse)
  override val get = urlHelper.get
  override val doesUrlMatchHost = urlHelper.doesUrlMatchHost
}

private object AzLyricsRetriever {
  @VisibleForTesting
  private[retrievers] val url: SingleHostUrl = new SingleHostUrl {
    private def normalize(s: String): String = s.filter(e => e.isDigit || e.isLetter).toLowerCase

    override val hostPrefix: String = "https://www.azlyrics.com/lyrics"
    override def urlFor(s: Song) =
      s"$hostPrefix/${normalize(s.artistName)}/${normalize(s.title)}.html"
  }
  @VisibleForTesting
  private[retrievers] val parser: SingleHostParser = new SingleHostParser {
    // AZ lyrics don't support instrumental :\
    override def apply(d: Document, s: Song): LyricParseResult = LyricParseResult.Lyrics(
      d.selectIterator(".main-page .text-center div:not([class]):not([id])")
        .map(_.wholeText)
        .filter(_.nonEmpty)
        .single
        .trim
        .|>(HtmlLyricsUtils.addBreakLines),
    )
    override val source = "AZLyrics"
  }
}
