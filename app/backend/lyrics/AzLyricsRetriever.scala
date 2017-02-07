package backend.lyrics

import common.io.InternetTalker
import common.rich.collections.RichTraversableOnce._
import models.Song
import org.jsoup.nodes.Document

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext

private class AzLyricsRetriever(implicit ec: ExecutionContext, it: InternetTalker) extends HtmlRetriever {
  private def normalize(s: String): String = s.filter(e => e.isDigit || e.isLetter).toLowerCase
  // AZ lyrics don't support instrumental :\
  override def fromHtml(html: Document, s: Song): Option[String] = Some(
    html.select(".main-page .text-center div")
        .toTraversable
        .filter(_.classNames.isEmpty)
        .single
        .html
        .replaceAll("<!--.*?-->\\s*", ""))
  override def getUrl(s: Song) =
    s"http://www.azlyrics.com/lyrics/${normalize(s.artistName)}/${normalize(s.title)}.html"
  override protected val source = "AZLyrics"
}
