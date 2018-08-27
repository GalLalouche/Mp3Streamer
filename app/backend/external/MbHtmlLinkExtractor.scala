package backend.external

import backend.Url
import backend.configs.Configuration
import backend.recon.{Album, Artist, Reconcilable, ReconID}
import common.io.InternetTalker
import common.rich.RichT._
import net.codingwell.scalaguice.InjectorExtensions._
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConverters._
import scala.concurrent.Future

private sealed class MbHtmlLinkExtractor[T <: Reconcilable](metadataType: String)(implicit c: Configuration)
    extends ExternalLinkProvider[T] {
  private implicit val it: InternetTalker = c.injector.instance[InternetTalker]
  private def getMbUrl(reconId: ReconID): Url = Url(s"https://musicbrainz.org/$metadataType/${reconId.id}")
  private def extractLink(e: Element): BaseLink[T] = {
    val url: Url = Url(e.select("a").attr("href").mapIf(_.startsWith("//")).to("https:" + _))
    val sourceName = e.className
        .takeWhile(_ != '-')
        .mapIf(_ == "no").to(e.child(0).text().const)
    BaseLink(url, Host fromUrl url getOrElse Host(sourceName, url.host))
  }
  private def extractLinks(d: Document): List[BaseLink[T]] =
    d.select(".external_links")
        .select("li").asScala
        .filterNot(_.className() == "all-relationships")
        .map(extractLink)
        .toList

  override def apply(id: ReconID): Future[Traversable[BaseLink[T]]] = {
    val mbLink = BaseLink[T](getMbUrl(id), Host.MusicBrainz)
    it.downloadDocument(getMbUrl(id)).map(extractLinks).map(mbLink :: _)
  }
}

private class ArtistLinkExtractor(implicit c: Configuration) extends MbHtmlLinkExtractor[Artist]("artist")
private class AlbumLinkExtractor(implicit c: Configuration) extends MbHtmlLinkExtractor[Album]("release-group")

