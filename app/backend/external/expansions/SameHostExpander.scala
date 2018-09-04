package backend.external.expansions

import backend.external.{BaseLink, Host}
import backend.external.recons.LinkRetriever
import backend.recon.{Album, Artist}

import scala.concurrent.Future

/** E.g., from an artist's wikipedia page, to that artists' wikipedia pages of her albums */
private trait SameHostExpander {
  def host: Host

  // For point free style.
  def apply: (BaseLink[Artist], Album) => Future[Option[BaseLink[Album]]]
  def toReconciler(artistLinks: BaseLink[Artist]): LinkRetriever[Album] = new LinkRetriever[Album](host) {
    override def apply(a: Album): Future[Option[BaseLink[Album]]] =
      SameHostExpander.this.apply(artistLinks, a)
  }
}
