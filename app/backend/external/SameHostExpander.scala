package backend.external

import backend.Url
import backend.recon.Reconcilable
import backend.storage.Retriever
import common.io.DocumentDownloader
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

/** E.g., from an artist's wikipedia page, to that artists wikipedia pages of her albums */
private abstract class SameHostExpander[T <: Reconcilable, S <: Reconcilable](val host: Host)
                                                                             (implicit ec: ExecutionContext)
    extends Retriever[ExternalLink[T], Links[S]] {
  protected def aux(d: Document): Traversable[Url]

  override def apply(v1: ExternalLink[T]): Future[Links[S]] =
    DocumentDownloader(v1.link).map(aux).map(_.map(ExternalLink[S](_, host)))
}
