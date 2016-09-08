package backend.external.expansions

import backend.Url
import backend.external.recons.Reconciler
import backend.external.{ExternalLink, Host}
import backend.recon.{Album, Artist}
import common.io.InternetTalker
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

/** E.g., from an artist's wikipedia page, to that artists' wikipedia pages of her albums */
private abstract class SameHostExpander(val host: Host)(implicit ec: ExecutionContext, it: InternetTalker)
    extends ((ExternalLink[Artist], Album) => Future[Option[ExternalLink[Album]]]) {
  protected def findAlbum(d: Document, a: Album): Option[Url]

  protected def fromUrl(u: Url, a: Album): Future[Option[ExternalLink[Album]]] =
    it.downloadDocument(u).map(findAlbum(_, a).map(ExternalLink[Album](_, host)))

  override def apply(e: ExternalLink[Artist], a: Album) = fromUrl(e.ensuring(_.host == host).link, a)
  def toReconciler(artistLinks: ExternalLink[Artist]): Reconciler[Album] = new Reconciler[Album](host) {
    override def apply(a: Album): Future[Option[ExternalLink[Album]]] = SameHostExpander.this.apply(artistLinks, a)
  }
}
