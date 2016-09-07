package backend.external.expansions

import backend.external.recons.Reconciler
import backend.external.{ExternalLink, _}
import backend.recon.{Album, Artist}
import common.io.InternetTalker
import common.rich.collections.RichTraversableOnce._

import scala.concurrent.{ExecutionContext, Future}

/** E.g., from an artist's wikipedia page, to that artists' wikipedia pages of her albums */
private[external] class CompositeSameHostExpander private(cb: HostMap[SameHostExpander])(implicit ec: ExecutionContext)
    extends ((Links[Artist], Album) => Future[Links[Album]]) {
  def this(expanders: SameHostExpander*)(implicit ec: ExecutionContext) = this(expanders.mapBy(_.host))

  def apply(e: ExternalLink[Artist], a: Album): Future[Option[ExternalLink[Album]]] =
    cb.get(e.host)
        .map(_.apply(e, a))
        .getOrElse(Future successful None)

  override def apply(v1: Links[Artist], a: Album): Future[Links[Album]] =
    Future sequence v1.map(apply(_, a)) map (_.flatten)
  def toReconcilers(ls: Links[Artist]): Traversable[Reconciler[Album]] = {
    val availableHosts = ls.toMultiMap(_.host).mapValues(_.head)
    cb.flatMap(e => availableHosts.get(e._1).map(e._2.toReconciler))
  }
}

private[external] object CompositeSameHostExpander {
  def default(implicit ec: ExecutionContext, it: InternetTalker) = new CompositeSameHostExpander(new WikipediaAlbumFinder())
  // MetalArchives is commented until I get unbanned :|
}
