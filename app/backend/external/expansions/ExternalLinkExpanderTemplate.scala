package backend.external.expansions

import backend.external.{BaseLink, BaseLinks, Host}
import backend.recon.Reconcilable
import common.io.InternetTalker
import org.jsoup.nodes.Document

import scala.concurrent.Future

/** Downloads the jsoup documents and parses it */
private abstract class ExternalLinkExpanderTemplate[T <: Reconcilable](override val sourceHost: Host,
                                                                       override val potentialHostsExtracted: Traversable[Host])
                                                                      (implicit it: InternetTalker)
    extends ExternalLinkExpander[T] {
  protected def parseDocument(d: Document): BaseLinks[T]
  override def apply(l: BaseLink[T]): Future[BaseLinks[T]] = it downloadDocument l.link map parseDocument
}
