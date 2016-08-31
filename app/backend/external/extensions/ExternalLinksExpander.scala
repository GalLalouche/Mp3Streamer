package backend.external.extensions

import backend.external.{ExternalLink, _}
import backend.recon.{Album, Reconcilable}
import backend.Retriever
import common.io.InternetTalker

import scala.concurrent.{ExecutionContext, Future}

class ExternalLinksExpander[T <: Reconcilable](map: Map[Host, ExternalLinkExpander[T]])
                                              (implicit ec: ExecutionContext)
    extends Retriever[Links[T], Links[T]] {
  def this(es: ExternalLinkExpander[T]*)(implicit ec: ExecutionContext) = this(es.map(e => e.host -> e).toMap)
  private def apply(e: ExternalLink[T]): Future[Links[T]] =
    map.get(e.host)
        .map(_(e))
        .getOrElse(Future successful Nil)
  override def apply(es: Links[T]): Future[Links[T]] =
    Future sequence es.map(apply) map (_.flatten)
}
class AlbumLinksExpander(implicit ec: ExecutionContext, internetTalker: InternetTalker)
  extends ExternalLinksExpander[Album](new WikipediaAlbumExternalLinksExpander)
