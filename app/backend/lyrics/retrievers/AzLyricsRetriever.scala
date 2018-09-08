package backend.lyrics.retrievers

import com.google.common.annotations.VisibleForTesting
import common.io.InternetTalker
import common.rich.collections.RichTraversableOnce._
import javax.inject.Inject
import models.Song
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

private[lyrics] class AzLyricsRetriever @Inject()(
    it: InternetTalker,
    singleHostHelper: SingleHostParsingHelper,
) extends SingleHostHtmlRetriever(it) {
  private def normalize(s: String): String = s.filter(e => e.isDigit || e.isLetter).toLowerCase
  override protected val hostPrefix: String = "https://www.azlyrics.com/lyrics"
  override def getUrl(s: Song) = s"$hostPrefix/${normalize(s.artistName)}/${normalize(s.title)}.html"

  @VisibleForTesting
  private[retrievers] val parser = new SingleHostUrlHelper {
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
  override val parse = singleHostHelper(parser)
}
