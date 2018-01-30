package backend.external.expansions

import backend.external._
import backend.external.recons.Reconciler
import backend.recon.{Album, Artist}
import common.io.InternetTalker
import common.rich.collections.RichTraversableOnce._
import common.rich.func.{MoreTraverseInstances, ToMoreFoldableOps}

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Traverse
import scalaz.std.{FutureInstances, OptionInstances}
import scalaz.syntax.ToTraverseOps

/** E.g., from an artist's wikipedia page, to that artists' wikipedia pages of her albums */
private[external] class CompositeSameHostExpander private(expanders: HostMap[SameHostExpander])(implicit ec: ExecutionContext)
    extends ToTraverseOps with MoreTraverseInstances with ToMoreFoldableOps
        with OptionInstances with FutureInstances {
  def this(expanders: SameHostExpander*)(implicit ec: ExecutionContext) = this(expanders.mapBy(_.host))

  def apply(link: BaseLink[Artist], a: Album): Future[Option[BaseLink[Album]]] =
    expanders.get(link.host).mapHeadOrElse(_.apply(link, a), Future successful None)

  def apply(links: BaseLinks[Artist], a: Album): Future[BaseLinks[Album]] =
    Traverse[Traversable].traverse(links)(apply(_, a)).map(_.flatten)
  def toReconcilers(ls: BaseLinks[Artist]): Traversable[Reconciler[Album]] = {
    val availableHosts = ls.toMultiMap(_.host).mapValues(_.head)
    expanders.flatMap(e => availableHosts.get(e._1).map(e._2.toReconciler))
  }
}

private[external] object CompositeSameHostExpander {
  def default(implicit it: InternetTalker) =
    new CompositeSameHostExpander(new WikipediaAlbumFinder(), new AllMusicAlbumFinder())
  // MetalArchives is not-production ready until I get unbanned :|
}
