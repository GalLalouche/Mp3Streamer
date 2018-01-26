package backend.lyrics.retrievers

import backend.Url
import backend.lyrics.{HtmlLyrics, Instrumental, Lyrics}
import common.io.InternetTalker
import common.rich.primitives.RichBoolean._
import models.Song
import org.jsoup.nodes.Document

import scala.concurrent.Future

private[lyrics] abstract class SingleHostHtmlRetriever(implicit it: InternetTalker) extends HtmlRetriever {
  // return None if instrumental
  protected def fromHtml(html: Document, s: Song): Option[String]
  protected def getUrl(s: Song): String
  protected val source: String
  protected val hostPrefix: String

  override def apply(s: Song): Future[Lyrics] = parse(Url(getUrl(s)), s)
  override def doesUrlMatchHost(url: Url): Boolean = url.address.startsWith(hostPrefix)
  override def parse(url: Url, s: Song): Future[Lyrics] = {
    it.downloadDocument(url)
        .map(e => fromHtml(e, s))
        .map(_.map(HtmlLyrics(source, _)).getOrElse(Instrumental(source)))
        .filter {
          case HtmlLyrics(_, h) => h.matches("[\\s<br>/]*").isFalse
          case _ => true
        }
  }
}
