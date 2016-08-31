package backend.external.expansions

import backend.Retriever
import backend.external.{Host, _}
import backend.recon.Reconcilable

/** E.g., from wikipedia to allmusic */
private[external] trait ExternalLinkExpander[T <: Reconcilable] extends Retriever[ExternalLink[T], Links[T]] {
  /** The host links are extracted from */
  def sourceHost: Host
  /**
   * Possible links that can be extracted. Note that not all links listed will be extracted, since they
   * might not exist in the source's document. Therefore, this only lists hosts that *can* be extracted.
   */
  def potentialHostsExtracted: Traversable[Host]
}
