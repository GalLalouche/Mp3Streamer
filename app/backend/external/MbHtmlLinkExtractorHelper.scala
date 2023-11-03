package backend.external

import backend.recon.{Reconcilable, ReconID}
import backend.Url
import javax.inject.Inject
import org.jsoup.nodes.{Document, Element}

import scala.concurrent.{ExecutionContext, Future}

import common.io.InternetTalker
import common.rich.RichT._
import common.RichJsoup._

/** Because MusicBrainz's API *still* doesn't allow for url-rel in release-groups. */
private[backend] class MbHtmlLinkExtractorHelper @Inject() (it: InternetTalker) {
  private implicit val iec: ExecutionContext = it
  def apply[R <: Reconcilable](metadataType: String)(id: ReconID): Future[BaseLinks[R]] = {
    def extractLink(e: Element): Option[BaseLink[R]] = {
      val url = Url(e.selectSingle("a").href.mapIf(_.startsWith("//")).to("https:" + _))
      Host.withUrl(url).map(BaseLink(url, _))
    }
    def extractLinks(d: Document): List[BaseLink[R]] = d
      .selectIterator(".external_links li")
      .filterNot(_.className == "all-relationships")
      .flatMap(extractLink)
      .toList

    val mbUrl: Url = Url(s"https://musicbrainz.org/$metadataType/${id.id}")
    val mbLink = BaseLink[R](mbUrl, Host.MusicBrainz)
    it.downloadDocument(mbUrl).map(extractLinks).map(mbLink :: _)
  }
}
