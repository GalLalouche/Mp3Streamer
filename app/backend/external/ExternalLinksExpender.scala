package backend.external

import backend.recon.{Album, Reconcilable}
import backend.storage.Retriever
import common.io.DocumentDownloader
import common.rich.RichT._

import scala.concurrent.{ExecutionContext, Future}

class ExternalLinksExpender[T <: Reconcilable](map: Map[Host, ExternalLinkExpender[T]])
                                              (implicit ec: ExecutionContext)
    extends Retriever[Traversable[ExternalLink[T]], Traversable[ExternalLink[T]]] {
  def this(es: ExternalLinkExpender[T]*)(implicit ec: ExecutionContext) = this(es.map(e => e.host -> e).toMap)
  private def apply(e: ExternalLink[T]): Future[Traversable[ExternalLink[T]]] =
    map.get(e.host)
        .map(r => DocumentDownloader(e.link.log("Link is " + _)).map(r))
        .getOrElse(Future successful Nil)
  override def apply(es: Traversable[ExternalLink[T]]): Future[Traversable[ExternalLink[T]]] =
    Future sequence es.map(apply) map (_.flatten)
}
class AlbumLinksExpender(implicit ec: ExecutionContext) extends ExternalLinksExpender[Album](WikipediaAlbumExternalLinksExpender)
