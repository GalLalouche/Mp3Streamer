package backend.external.recons

import backend.recon.{Album, Artist, Reconcilable}
import javax.inject.Inject

private[external] case class LinkRetrievers[R <: Reconcilable](get: Traversable[LinkRetriever[R]]) {
  def ++(other: Traversable[LinkRetriever[R]]): LinkRetrievers[R] = LinkRetrievers(get ++ other)
  def ++(other: LinkRetrievers[R]): LinkRetrievers[R] = ++(other.get)
}
private[external] class ArtistLinkRetrievers @Inject()(lastFmReconciler: LastFmLinkRetriever)
    extends LinkRetrievers[Artist](Vector(lastFmReconciler))
private[external] class AlbumLinkRetrievers @Inject() extends LinkRetrievers[Album](Nil)

