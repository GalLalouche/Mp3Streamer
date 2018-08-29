package backend.lyrics.retrievers

import backend.configs.Configuration
import common.io.InternetTalker
import common.rich.collections.RichTraversableOnce._
import models.Song
import org.jsoup.nodes.Document
import net.codingwell.scalaguice.InjectorExtensions._

import scala.collection.JavaConverters._

private[lyrics] class AzLyricsRetriever(implicit c: Configuration)
    extends SingleHostHtmlRetriever(c.injector.instance[InternetTalker]) {
  private def normalize(s: String): String = s.filter(e => e.isDigit || e.isLetter).toLowerCase
  // AZ lyrics don't support instrumental :\
  override def fromHtml(html: Document, s: Song): Option[String] = Some(
    html.select(".main-page .text-center div").asScala
        .filter(_.classNames.isEmpty)
        .single
        .html
        // TODO replace with parsec
        .replaceAll("<!--.*?-->\\s*", ""))
  override protected val hostPrefix: String = "https://www.azlyrics.com/lyrics"
  override def getUrl(s: Song) = s"$hostPrefix/${normalize(s.artistName)}/${normalize(s.title)}.html"
  override protected val source = "AZLyrics"
}
