package backend.lyrics.retrievers

import backend.Url
import backend.lyrics.Lyrics
import common.io.InternetTalker
import common.rich.func.ToMoreFoldableOps
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.OptionInstances

// TODO replace with composition
private[lyrics] abstract class SingleHostHtmlRetriever(it: InternetTalker) extends HtmlRetriever
    with ToMoreFoldableOps with OptionInstances {
  private implicit val iec: ExecutionContext = it
  protected def getUrl(s: Song): String
  protected val hostPrefix: String

  override def apply(s: Song): Future[Lyrics] = parse(Url(getUrl(s)), s)
  override def doesUrlMatchHost(url: Url): Boolean = url.address.startsWith(hostPrefix)
}
