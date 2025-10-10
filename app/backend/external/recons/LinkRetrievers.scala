package backend.external.recons

import backend.recon.Reconcilable

private[external] class LinkRetrievers[R <: Reconcilable] private (val get: Seq[LinkRetriever[R]])
    extends AnyVal {
  def ++(other: Iterable[LinkRetriever[R]]): LinkRetrievers[R] = LinkRetrievers(get ++ other)
  def ++(other: LinkRetrievers[R]): LinkRetrievers[R] = ++(other.get)
}

private[external] object LinkRetrievers {
  def apply[R <: Reconcilable](retrievers: IterableOnce[LinkRetriever[R]]): LinkRetrievers[R] =
    new LinkRetrievers(retrievers.iterator.toVector.sortBy(_.qualityRank))
  def apply[R <: Reconcilable](lrs: LinkRetriever[R]*): LinkRetrievers[R] = new LinkRetrievers(lrs)
}
