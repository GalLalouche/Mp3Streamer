package backend.external.expansions

import backend.external.{ExternalLink, _}
import backend.recon.{Album, Artist}
import common.rich.collections.RichTraversableOnce._

import scala.concurrent.{ExecutionContext, Future}

/** E.g., from an artist's wikipedia page, to that artists' wikipedia pages of her albums */
private class CompositeSameHostExpander private(cb: HostMap[SameHostExpander])(implicit ec: ExecutionContext)
    extends ((Links[Artist], Album) => Future[Links[Album]]) {
  def this(expanders: SameHostExpander*)(implicit ec: ExecutionContext) = this(expanders.mapBy(_.host))

  def apply(e: ExternalLink[Artist], a: Album): Future[Option[ExternalLink[Album]]] =
    cb.get(e.host)
        .map(_.apply(e, a))
        .getOrElse(Future successful None)

  override def apply(v1: Links[Artist], a: Album): Future[Links[Album]] =
    Future sequence v1.map(apply(_, a)) map (_.flatten)
}

object CompositeSameHostExpander {
  def default(implicit ec: ExecutionContext): ((Links[Artist], Album) => Future[Links[Album]]) =
    new CompositeSameHostExpander(// new MetalArchivesAlbumsFinder temporarily disabled, until I figure out WTF is going on with this site
    )
}
