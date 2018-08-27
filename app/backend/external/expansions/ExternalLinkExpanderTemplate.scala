package backend.external.expansions

import backend.configs.Configuration
import backend.external.{BaseLink, BaseLinks, Host}
import backend.recon.Reconcilable
import common.io.InternetTalker
import net.codingwell.scalaguice.InjectorExtensions._
import org.jsoup.nodes.Document

import scala.concurrent.Future

/** Downloads the jsoup documents and parses it */
private abstract class ExternalLinkExpanderTemplate[T <: Reconcilable](
    override val sourceHost: Host,
    override val potentialHostsExtracted: Traversable[Host],
)(implicit c: Configuration)
    extends ExternalLinkExpander[T] {
  private implicit val it: InternetTalker = c.injector.instance[InternetTalker]
  protected def parseDocument(d: Document): BaseLinks[T]
  override def apply(l: BaseLink[T]): Future[BaseLinks[T]] = it downloadDocument l.link map parseDocument
}
