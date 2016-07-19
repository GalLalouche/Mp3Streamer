package backend.mb

import backend.Url
import backend.external.{ExternalLink, ExternalLinkProvider}
import backend.recon.ReconID
import common.io.DocumentDownloader
import common.rich.RichT._
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class MbHtmlLinkExtractor(implicit ec: ExecutionContext) extends ExternalLinkProvider {
  private def getHtml(artistId: String): Future[Document] =
    DocumentDownloader(Url("https://musicbrainz.org/artist/" + artistId))

  private def extractLink(e: Element): ExternalLink = {
    val url: Url = Url(e.child(0).attr("href"))
    val sourceName = e.className
      .takeWhile(_ != '-')
      .mapIf(_ == "no").to(x => e.child(0).text())
    ExternalLink(url, Host(sourceName, url.host))
  }
  def extractLinks(d: Document): Seq[ExternalLink] =
    d.select(".external_links")
      .select("li")
      .dropRight(1)
      .map(extractLink)

  override def apply(id: ReconID): Future[Traversable[ExternalLink]] =
    getHtml(id.id).map(extractLinks)
}
