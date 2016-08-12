package backend.external

import backend.recon.Reconcilable
import backend.storage.Retriever
import common.io.DocumentDownloader
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

/** E.g., from wikipedia to allmusic */
private abstract class ExternalLinkExpander[T <: Reconcilable](val host: Host)(implicit ec: ExecutionContext) extends Retriever[ExternalLink[T], Links[T]] {
  protected def aux(d: Document): Links[T]

  override def apply(v1: ExternalLink[T]): Future[Links[T]] = DocumentDownloader(v1.link).map(aux)
}
