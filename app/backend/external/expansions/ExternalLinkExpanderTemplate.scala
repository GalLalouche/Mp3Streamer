package backend.external.expansions

import backend.external.{BaseLink, BaseLinks, Host}
import backend.recon.Reconcilable
import common.io.InternetTalker
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

/** Downloads the jsoup documents and parses it */
// TODO replace with composition
private abstract class ExternalLinkExpanderTemplate[R <: Reconcilable](
    override val sourceHost: Host,
    override val potentialHostsExtracted: Traversable[Host],
    it: InternetTalker,
) extends ExternalLinkExpander[R] {
  private implicit val iec: ExecutionContext = it
  protected def parseDocument(d: Document): BaseLinks[R]
  override def apply(l: BaseLink[R]): Future[BaseLinks[R]] = it downloadDocument l.link map parseDocument
}
