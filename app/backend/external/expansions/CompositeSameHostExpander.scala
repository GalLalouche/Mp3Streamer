package backend.external.expansions

import backend.external.recons.Reconciler
import backend.external._
import backend.recon.{Album, Artist}
import common.io.InternetTalker
import common.rich.collections.RichTraversableOnce._
import common.rich.func.MoreTraverse._

import scala.concurrent.{ExecutionContext, Future}
import scalaz.std.FutureInstances
import scalaz.syntax.ToTraverseOps

/** E.g., from an artist's wikipedia page, to that artists' wikipedia pages of her albums */
private[external] class CompositeSameHostExpander private(cb: HostMap[SameHostExpander])(implicit ec: ExecutionContext)
    extends ((BaseLinks[Artist], Album) => Future[BaseLinks[Album]])
        with FutureInstances with ToTraverseOps {
  def this(expanders: SameHostExpander*)(implicit ec: ExecutionContext) = this(expanders.mapBy(_.host))

  def apply(e: BaseLink[Artist], a: Album): Future[Option[BaseLink[Album]]] =
    cb.get(e.host)
        .map(_.apply(e, a))
        .getOrElse(Future successful None)

  override def apply(v1: BaseLinks[Artist], a: Album): Future[BaseLinks[Album]] =
    v1.toTraversable.traverse(apply(_, a)) map (_.flatten)
  def toReconcilers(ls: BaseLinks[Artist]): Traversable[Reconciler[Album]] = {
    val availableHosts = ls.toMultiMap(_.host).mapValues(_.head)
    cb.flatMap(e => availableHosts.get(e._1).map(e._2.toReconciler))
  }
}

private[external] object CompositeSameHostExpander {
  def default(implicit ec: ExecutionContext, it: InternetTalker) =
    new CompositeSameHostExpander(new WikipediaAlbumFinder(), new AllMusicAlbumFinder())
  // MetalArchives is not-production ready until I get unbanned :|
}
