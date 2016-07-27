package backend.mb

import backend.Url
import backend.external.{ExternalLink, ExternalLinkProvider, Host}
import backend.recon.{Album, Artist, ReconID, Reconcilable}
import common.io.DocumentDownloader
import common.rich.RichT._
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}

private sealed class MbHtmlLinkExtractor[T <: Reconcilable](metadataType: String)(implicit ec: ExecutionContext)
    extends ExternalLinkProvider[T] {
  private[mb] def getHtml(artistId: String): Future[Document] =
    DocumentDownloader(Url(s"https://musicbrainz.org/$metadataType/$artistId"))
  private def extractLink(e: Element): ExternalLink[T] = {
    val url: Url = Url(e.child(0).attr("href").mapIf(_.startsWith("//")).to("https:" + _))
    val sourceName = e.className
        .takeWhile(_ != '-')
        .mapIf(_ == "no").to(x => e.child(0).text())
    ExternalLink(url, Host(sourceName, url.host))
  }
  def extractLinks(d: Document): Seq[ExternalLink[T]] =
    d.select(".external_links")
        .select("li")
        .filterNot(_.className() == "all-relationships")
        .map(extractLink)

  override def apply(id: ReconID): Future[Traversable[ExternalLink[T]]] =
    getHtml(id.id).map(extractLinks)
}

private class ArtistLinkExtractor(implicit ec: ExecutionContext) extends MbHtmlLinkExtractor[Artist]("artist")
private class AlbumLinkExtractor(implicit ec: ExecutionContext) extends MbHtmlLinkExtractor[Album]("release-group")

