package backend.lyrics.retrievers

import backend.Url
import backend.lyrics.{HtmlLyrics, Instrumental, Lyrics}
import common.io.InternetTalker
import common.rich.func.ToMoreFoldableOps
import common.rich.primitives.RichBoolean._
import models.Song
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.OptionInstances

// TODO replace with composition
private[lyrics] abstract class SingleHostHtmlRetriever(it: InternetTalker) extends HtmlRetriever
    with ToMoreFoldableOps with OptionInstances {
  private implicit val iec: ExecutionContext = it
  private[retrievers] def fromHtml(html: Document, s: Song): LyricParseResult
  protected def getUrl(s: Song): String
  protected val source: String
  protected val hostPrefix: String

  override def apply(s: Song): Future[Lyrics] = parse(Url(getUrl(s)), s)
  override def doesUrlMatchHost(url: Url): Boolean = url.address.startsWith(hostPrefix)
  override def parse(url: Url, s: Song): Future[Lyrics] = {
    it.downloadDocument(url)
        .map(fromHtml(_, s))
        .map {
          case LyricParseResult.Instrumental => Instrumental(source)
          case LyricParseResult.Lyrics(l) => HtmlLyrics(source, l)
          case LyricParseResult.Error(e) => throw e
        }
        .filter {
          case HtmlLyrics(_, h) => h.matches("[\\s<br>/]*").isFalse
          case _ => true
        }
  }
}
