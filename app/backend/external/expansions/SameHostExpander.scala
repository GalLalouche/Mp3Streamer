package backend.external.expansions

import backend.FutureOption
import backend.external.{BaseLink, Host}
import backend.external.recons.LinkRetriever
import backend.recon.{Album, Artist}

/** E.g., from an artist's wikipedia page, to that artists' wikipedia pages of her albums */
private trait SameHostExpander {
  def host: Host

  // For point free style.
  def apply: (BaseLink[Artist], Album) => FutureOption[BaseLink[Album]]
  def toReconciler(artistLinks: BaseLink[Artist]): LinkRetriever[Album] = new LinkRetriever[Album] {
    override def host = SameHostExpander.this.host
    override def apply(a: Album) = SameHostExpander.this.apply(artistLinks, a)
  }
}
