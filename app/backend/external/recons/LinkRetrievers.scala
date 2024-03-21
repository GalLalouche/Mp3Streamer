package backend.external.recons

import backend.recon.Reconcilable

private[external] class LinkRetrievers[R <: Reconcilable] private (val get: Seq[LinkRetriever[R]])
    extends AnyVal {
  def ++(other: Traversable[LinkRetriever[R]]): LinkRetrievers[R] = LinkRetrievers(get ++ other)
  def ++(other: LinkRetrievers[R]): LinkRetrievers[R] = ++(other.get)
}

private[external] object LinkRetrievers {
  def apply[R <: Reconcilable](seq: TraversableOnce[LinkRetriever[R]]): LinkRetrievers[R] =
    new LinkRetrievers(seq.toVector.sortBy(_.qualityRank))
  def apply[R <: Reconcilable](lrs: LinkRetriever[R]*): LinkRetrievers[R] =
    new LinkRetrievers(lrs)
}
