package backend.external.expansions

import backend.Url
import backend.external.recons.Reconciler
import backend.external.{BaseLink, Host}
import backend.recon.{Album, Artist}
import common.io.InternetTalker
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

/** E.g., from an artist's wikipedia page, to that artists' wikipedia pages of her albums */
private abstract class SameHostExpander(val host: Host)(implicit ec: ExecutionContext, it: InternetTalker)
    extends ((BaseLink[Artist], Album) => Future[Option[BaseLink[Album]]]) {
  protected def findAlbum(d: Document, a: Album): Option[Url]

  protected def fromUrl(u: Url, a: Album): Future[Option[BaseLink[Album]]] =
    it.downloadDocument(u).map(findAlbum(_, a).map(BaseLink[Album](_, host)))

  override def apply(e: BaseLink[Artist], a: Album) = fromUrl(e.ensuring(_.host == host).link, a)
  def toReconciler(artistLinks: BaseLink[Artist]): Reconciler[Album] = new Reconciler[Album](host) {
    override def apply(a: Album): Future[Option[BaseLink[Album]]] = SameHostExpander.this.apply(artistLinks, a)
  }
}
