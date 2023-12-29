package backend.external.expansions

import javax.inject.Inject

import backend.external.{BaseLink, BaseLinks}
import backend.recon.Reconcilable
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

import common.io.InternetTalker

/** Downloads the jsoup documents and parses it */
private class ExternalLinkExpanderHelper @Inject() (it: InternetTalker) {
  private implicit val iec: ExecutionContext = it
  def apply[R <: Reconcilable](parse: Document => BaseLinks[R])(
      l: BaseLink[R],
  ): Future[BaseLinks[R]] = it.downloadDocument(l.link).map(parse)
}
