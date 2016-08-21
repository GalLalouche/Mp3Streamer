package backend.external

import backend.recon.Reconcilable
import backend.storage.Retriever
import common.io.{DocumentDownloader, InternetTalker}
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

/** E.g., from wikipedia to allmusic */
private abstract class ExternalLinkExpander[T <: Reconcilable](val host: Host)(implicit ec: ExecutionContext, it: InternetTalker)
    extends Retriever[ExternalLink[T], Links[T]] {
  protected def aux(d: Document): Links[T]

  override def apply(v1: ExternalLink[T]): Future[Links[T]] = it.downloadDocument(v1.link).map(aux)
}
