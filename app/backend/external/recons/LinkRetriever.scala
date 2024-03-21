package backend.external.recons

import backend.OptionRetriever
import backend.external.{BaseLink, Host}
import backend.recon.Reconcilable

private[external] trait LinkRetriever[R <: Reconcilable] extends OptionRetriever[R, BaseLink[R]] {
  // A low value means the retriever is more accurate.
  def qualityRank: Int
  def host: Host
}
private[external] object LinkRetriever {
  implicit def ordering[R <: Reconcilable]: Ordering[LinkRetriever[R]] = Ordering.by(_.qualityRank)
}
