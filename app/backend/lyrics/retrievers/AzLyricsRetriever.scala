package backend.lyrics.retrievers

import com.google.common.annotations.VisibleForTesting
import common.rich.collections.RichTraversableOnce._
import javax.inject.Inject
import models.Song
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

private[lyrics] class AzLyricsRetriever @Inject()(singleHostHelper: SingleHostParsingHelper)
    extends HtmlRetriever {
  import AzLyricsRetriever._

  override val parse = singleHostHelper(parser)

  private val urlHelper = new SingleHostUrlHelper(url, parse)
  override val get = urlHelper.get
  override val doesUrlMatchHost = urlHelper.doesUrlMatchHost
}

private object AzLyricsRetriever {
  @VisibleForTesting
  private[retrievers] val url = new SingleHostUrl {
    private def normalize(s: String): String = s.filter(e => e.isDigit || e.isLetter).toLowerCase

    override val hostPrefix: String = "https://www.azlyrics.com/lyrics"
    override def urlFor(s: Song) = s"$hostPrefix/${normalize(s.artistName)}/${normalize(s.title)}.html"
  }
  @VisibleForTesting
  private[retrievers] val parser = new SingleHostParser {
    // AZ lyrics don't support instrumental :\
    override def apply(d: Document, s: Song): LyricParseResult = LyricParseResult.Lyrics(
      d.select(".main-page .text-center div").asScala
          .filter(_.classNames.isEmpty)
          .single
          .html
          // TODO replace with parsec
          .replaceAll("<!--.*?-->\\s*", ""))
    override val source = "AZLyrics"
  }
}
