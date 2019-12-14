package backend.external

import backend.Url
import backend.recon.{Reconcilable, ReconID}
import javax.inject.Inject
import org.jsoup.nodes.{Document, Element}

import scala.concurrent.{ExecutionContext, Future}

import common.RichJsoup._
import common.io.InternetTalker
import common.rich.RichT._

private class MbHtmlLinkExtractorHelper @Inject()(it: InternetTalker) {
  private implicit val iec: ExecutionContext = it
  def apply[R <: Reconcilable](metadataType: String)(id: ReconID): Future[BaseLinks[R]] = {
    def extractLink(e: Element): BaseLink[R] = {
      val url = Url(e.selectSingle("a")
          .attr("href")
          .mapIf(_.startsWith("//")).to("https:" + _)
      )
      val sourceName = e.className
          .takeWhile(_ != '-')
          .mapIf(_ == "no").to(e.child(0).text().const)
      BaseLink(url, Host fromUrl url getOrElse Host(sourceName, url.host))
    }
    def extractLinks(d: Document): List[BaseLink[R]] = d
        .selectIterator(".external_links li")
        .filterNot(_.className() == "all-relationships")
        .map(extractLink)
        .toList

    val mbUrl: Url = Url(s"https://musicbrainz.org/$metadataType/${id.id}")
    val mbLink = BaseLink[R](mbUrl, Host.MusicBrainz)
    it.downloadDocument(mbUrl).map(extractLinks).map(mbLink :: _)
  }
}
