package backend.external.expansions

import backend.external.{BaseLink, BaseLinks}
import backend.external.recons.LinkRetrievers
import backend.recon.{Album, Artist}
import common.rich.collections.RichTraversableOnce._
import common.rich.func.{MoreTraverseInstances, ToMoreFoldableOps}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import scalaz.Traverse
import scalaz.std.{FutureInstances, OptionInstances}
import scalaz.syntax.ToTraverseOps

/** E.g., from an artist's wikipedia page, to that artists' wikipedia pages of her albums */
private[external] class CompositeSameHostExpander @Inject()(
    wiki: WikipediaAlbumFinder,
    am: AllMusicAlbumFinder,
    ec: ExecutionContext,
) extends ToTraverseOps with MoreTraverseInstances with ToMoreFoldableOps
    with OptionInstances with FutureInstances {
  private implicit val iec: ExecutionContext = ec

  private val expanders = Iterator(wiki, am).mapBy(_.host)

  def apply(link: BaseLink[Artist], a: Album): Future[Option[BaseLink[Album]]] =
    expanders.get(link.host).mapHeadOrElse(_.apply(link, a), Future successful None)

  def apply(links: BaseLinks[Artist], a: Album): Future[BaseLinks[Album]] =
    Traverse[Traversable].traverse(links)(apply(_, a)).map(_.flatten)
  def toReconcilers(ls: BaseLinks[Artist]): LinkRetrievers[Album] = {
    val availableHosts = ls.toMultiMap(_.host).mapValues(_.head)
    LinkRetrievers(expanders.flatMap(e => availableHosts.get(e._1).map(e._2.toReconciler)))
  }
}
