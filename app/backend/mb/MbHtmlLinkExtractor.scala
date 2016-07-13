package backend.mb

import backend.Url
import backend.external.{ExternalLink, ExternalLinksProvider}
import common.RichFuture._
import common.io.DocumentDownloader
import common.rich.RichT._
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MbHtmlLinkExtractor extends ExternalLinksProvider {
  private def toId(name: String): Future[Option[String]] = ReconcilerImpl.get(name).map(_._1)
  private def getHtml(artistId: String): Future[Document] =
    DocumentDownloader(Url("https://musicbrainz.org/artist/" + artistId))

  private def extractLink(e: Element): ExternalLink = {
    val url: Url = Url(e.child(0).attr("href"))
    val sourceName = e.className.takeWhile(_ != '-')
        .mapIf(_ == "no").to(x => e.child(0).text())
    ExternalLink(url, Host(sourceName, url.host))
  }
  def extractLinks(d: Document): Seq[ExternalLink] =
    d.select(".external_links")
        .log()
        .select("li")
        .dropRight(1)
        .map(extractLink)

  override def getExternalLinks(artistName: String): Future[Traversable[ExternalLink]] =
    toId(artistName)
        .filterWithMessage(_.isDefined, e => s"Could not find an ID for artist <${artistName}>")
        .map(_.get)
        .flatMap(getHtml)
        .map(extractLinks)

  def main(args: Array[String]) {
    getExternalLinks("Bruce Springsteen").get.log(_ mkString "\n")
  }
}
