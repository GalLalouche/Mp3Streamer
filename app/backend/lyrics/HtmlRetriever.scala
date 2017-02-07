package backend.lyrics

import backend.Url
import common.io.InternetTalker
import models.Song
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

private abstract class HtmlRetriever(implicit ec: ExecutionContext, it: InternetTalker) extends LyricsRetriever {
  // return None if instrumental
  protected def fromHtml(html: Document, s: Song): Option[String]
  protected def getUrl(s: Song): String
  protected val source: String
  override def apply(s: Song): Future[Lyrics] =
    it.downloadDocument(Url(getUrl(s)))
        .map(e => fromHtml(e, s))
        .map(_.map(HtmlLyrics(source, _)).getOrElse(Instrumental(source)))
        .filter {
          case HtmlLyrics(_, h) => false == h.matches("[\\s<br>/]*")
          case _ => true
        }
}
