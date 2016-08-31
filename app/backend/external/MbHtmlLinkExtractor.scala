package backend.external

import backend.Url
import backend.recon.{Album, Artist, ReconID, Reconcilable}
import common.io.DocumentDownloader
import common.rich.RichT._
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}

private sealed class MbHtmlLinkExtractor[T <: Reconcilable](metadataType: String)(implicit ec: ExecutionContext)
    extends ExternalLinkProvider[T] {
  private def getMbUrl(reconId: ReconID): Url = Url(s"https://musicbrainz.org/$metadataType/${reconId.id}")
  protected def getHtml(reconId: ReconID): Future[Document] =
    DocumentDownloader(getMbUrl(reconId))
  private def extractLink(e: Element): ExternalLink[T] = {
    val url: Url = Url(e.select("a").attr("href").mapIf(_.startsWith("//")).to("https:" + _))
    val sourceName = e.className
        .takeWhile(_ != '-')
        .mapIf(_ == "no").to(x => e.child(0).text())
    ExternalLink(url, Host fromUrl url getOrElse Host(sourceName, url.host))
  }
  def extractLinks(d: Document): List[ExternalLink[T]] =
    d.select(".external_links")
        .select("li")
        .filterNot(_.className() == "all-relationships")
        .map(extractLink)
        .toList

  override def apply(id: ReconID): Future[Traversable[ExternalLink[T]]] = {
    val mbLink = ExternalLink[T](getMbUrl(id), Host.MusicBrainz)
    getHtml(id).map(extractLinks).map(mbLink :: _)
  }
}

private class ArtistLinkExtractor(implicit ec: ExecutionContext) extends MbHtmlLinkExtractor[Artist]("artist")
private class AlbumLinkExtractor(implicit ec: ExecutionContext) extends MbHtmlLinkExtractor[Album]("release-group")

