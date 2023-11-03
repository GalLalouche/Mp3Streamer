package backend.external.expansions

import backend.external.{BaseLink, BaseLinks, Host}
import backend.recon.Reconcilable

import scala.concurrent.Future

/**
 * Expansions (not to be confused with ex<b>t</b>ansions) scrap a given link in search for more
 * links, e.g., they might find AllMusic links in a Wikipedia link.
 */
private[external] trait ExternalLinkExpander[R <: Reconcilable] {
  /** The host the links are extracted from. */
  def sourceHost: Host
  /**
   * Possible links that can be extracted. Note that not all links listed will be extracted, since
   * they might not exist in the source's document. Therefore, this only lists hosts that *can* be
   * extracted.
   */
  def potentialHostsExtracted: Traversable[Host]
  // For point free style.
  def expand: BaseLink[R] => Future[BaseLinks[R]]
}
