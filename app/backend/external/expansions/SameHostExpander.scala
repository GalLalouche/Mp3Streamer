package backend.external.expansions

import backend.Url
import backend.configs.Configuration
import backend.external.{BaseLink, Host}
import backend.external.recons.LinkRetriever
import backend.recon.{Album, Artist}
import common.io.InternetTalker
import net.codingwell.scalaguice.InjectorExtensions._
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

/** E.g., from an artist's wikipedia page, to that artists' wikipedia pages of her albums */
private abstract class SameHostExpander(val host: Host)(implicit c: Configuration) {
  protected def findAlbum(d: Document, a: Album): Future[Option[Url]]
  private implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
  private implicit val it: InternetTalker = c.injector.instance[InternetTalker]
  protected def fromUrl(u: Url, a: Album): Future[Option[BaseLink[Album]]] = for {
    d <- it.downloadDocument(u)
    a <- findAlbum(d, a)
  } yield a.map(BaseLink[Album](_, host))

  final def apply(e: BaseLink[Artist], a: Album): Future[Option[BaseLink[Album]]] =
    fromUrl(e.link, a)
  def toReconciler(artistLinks: BaseLink[Artist]): LinkRetriever[Album] = new LinkRetriever[Album](host) {
    override def apply(a: Album): Future[Option[BaseLink[Album]]] = SameHostExpander.this.apply(artistLinks, a)
  }
}
