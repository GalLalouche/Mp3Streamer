package backend.external.expansions

import backend.Url
import backend.external.recons.Reconciler
import backend.external.{BaseLink, Host}
import backend.recon.{Album, Artist}
import common.io.InternetTalker
import org.jsoup.nodes.Document

import scala.concurrent.Future

/** E.g., from an artist's wikipedia page, to that artists' wikipedia pages of her albums */
private abstract class SameHostExpander(val host: Host)(implicit it: InternetTalker) {
  protected def findAlbum(d: Document, a: Album): Future[Option[Url]]

  protected def fromUrl(u: Url, a: Album): Future[Option[BaseLink[Album]]] = for {
    d <- it.downloadDocument(u)
    a <- findAlbum(d, a)
  } yield a.map(BaseLink[Album](_, host))

  final def apply(e: BaseLink[Artist], a: Album): Future[Option[BaseLink[Album]]] =
    fromUrl(e.link, a)
  def toReconciler(artistLinks: BaseLink[Artist]): Reconciler[Album] = new Reconciler[Album](host) {
    override def apply(a: Album): Future[Option[BaseLink[Album]]] = SameHostExpander.this.apply(artistLinks, a)
  }
}
