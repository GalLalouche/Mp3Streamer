package lyrics

import models.Song
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

private abstract class HtmlRetriever(implicit ec: ExecutionContext) extends LyricsRetriever {
  protected def fromHtml(html: Document, s: Song): String
  protected def getUrl(s: Song): String
  protected val source: String
  override def apply(s: Song): Future[Lyrics] =
    Future.apply(scala.io.Source.fromURL(getUrl(s), "UTF-8"))
      .map(_.mkString)
      .map(e => fromHtml(Jsoup parse e, s))
      .map(Lyrics(_, source))
}
