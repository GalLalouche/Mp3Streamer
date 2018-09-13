package backend.external.recons

import backend.recon.Reconcilable

private[external] case class LinkRetrievers[R <: Reconcilable](get: Traversable[LinkRetriever[R]]) {
  def ++(other: Traversable[LinkRetriever[R]]): LinkRetrievers[R] = LinkRetrievers(get ++ other)
  def ++(other: LinkRetrievers[R]): LinkRetrievers[R] = ++(other.get)
}
