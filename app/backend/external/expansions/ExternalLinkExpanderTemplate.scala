package backend.external.expansions

import backend.external.{ExternalLink, Host, Links}
import backend.recon.Reconcilable
import common.io.InternetTalker
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

/** Downloads the jsoup documents and parses it */
private abstract class ExternalLinkExpanderTemplate[T <: Reconcilable](override val sourceHost: Host, override val potentialHostsExtracted: Traversable[Host])
                                                              (implicit ec: ExecutionContext, it: InternetTalker)
    extends ExternalLinkExpander[T] {
  protected def aux(d: Document): Links[T]
  override def apply(v1: ExternalLink[T]): Future[Links[T]] = it.downloadDocument(v1.link).map(aux)
}
