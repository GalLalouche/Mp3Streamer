package backend.external

import backend.recon.{Reconcilable, ReconID}
import com.google.inject.Inject
import io.lemonlabs.uri.Url
import org.jsoup.nodes.{Document, Element}

import scala.concurrent.{ExecutionContext, Future}

import common.RichJsoup._
import common.io.InternetTalker
import common.rich.RichT._

/** Because MusicBrainz's API *still* doesn't allow for url-rel in release-groups. */
private[backend] class MbHtmlLinkExtractorHelper @Inject() (
    it: InternetTalker,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec

  def apply[R <: Reconcilable](metadataType: String)(id: ReconID): Future[BaseLinks[R]] = {
    def extractLink(e: Element): Option[BaseLink[R]] = {
      val url = Url.parse(e.selectSingle("a").href.mapIf(_.startsWith("//")).to("https:" + _))
      Host.withUrl(url).map(BaseLink(url, _))
    }
    def extractLinks(d: Document): List[BaseLink[R]] = d
      .selectIterator(".external_links li")
      .filterNot(_.className == "all-relationships")
      .flatMap(extractLink)
      .toList

    val mbUrl: Url = Url.parse(s"https://musicbrainz.org/$metadataType/${id.id}")
    val mbLink = BaseLink[R](mbUrl, Host.MusicBrainz)
    it.downloadDocument(mbUrl).map(extractLinks).map(mbLink :: _)
  }
}
