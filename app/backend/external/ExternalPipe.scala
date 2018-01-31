package backend.external

import backend.Retriever
import backend.external.expansions.ExternalLinkExpander
import backend.external.recons.Reconciler
import backend.recon.{ReconID, Reconcilable}
import common.rich.collections.RichSet._
import common.rich.collections.RichTraversableOnce._
import common.rich.func.{MoreTraversableInstances, MoreTraverseInstances}

import scala.concurrent.{ExecutionContext, Future}
import scalaz.std.FutureInstances
import scalaz.syntax.{ToBindOps, ToTraverseOps}

/**
 * Encompasses all online steps for fetching the external link for a given entity.
 *
 * @param reconciler            Matches the entity to an API's ID
 * @param linksRetriever        Fetches the external links attached to the entity's ID
 * @param expanders             Attempts to expand the links found by scraping the initial set
 * @param additionalReconcilers Any additional, standalone reconciliations that can be performed
 */
//TODO recursively deep?
private class ExternalPipe[R <: Reconcilable](reconciler: Retriever[R, ReconID],
    linksRetriever: Retriever[ReconID, BaseLinks[R]],
    expanders: Traversable[ExternalLinkExpander[R]],
    additionalReconcilers: Traversable[Reconciler[R]])
    (implicit ec: ExecutionContext) extends Retriever[R, MarkedLinks[R]]
    with ToTraverseOps with ToBindOps
    with FutureInstances with MoreTraverseInstances with MoreTraversableInstances {
  override def apply(r: R): Future[MarkedLinks[R]] = reconciler(r) >>= linksRetriever >>= expand(r)

  private def expand(r: R)(existingLinks: BaseLinks[R]): Future[MarkedLinks[R]] = {
    def extractHosts(ls: BaseLinks[R]) = ls.map(_.host).toSet
    val existingHosts = extractHosts(existingLinks)
    for {
      newLinks <- applyNewHostExpanders(existingHosts, existingLinks)
      additionalLinks <- applyNewHostReconcilers(r, existingHosts ++ extractHosts(newLinks))
    } yield {
      val existingSet = existingLinks.toSet
      val newSet = newLinks.toSet ++ additionalLinks
      existingSet.map(MarkedLink.markExisting) ++
          filterLinksWithNewHosts(existingSet, newSet).map(MarkedLink.markNew)
    }
  }

  private def applyNewHostExpanders(
      existingHosts: Set[Host], links: Traversable[BaseLink[R]]): Future[BaseLinks[R]] = {
    val nextExpandersByHost =
      expanders.filterNot(_.potentialHostsExtracted.toSet <= existingHosts).mapBy(_.sourceHost)
    links.mproduct(nextExpandersByHost get _.host)
        .traverseM { case (link, expander) => expander(link) }
  }

  private def applyNewHostReconcilers(entity: R, existingHosts: Set[Host]): Future[BaseLinks[R]] =
    additionalReconcilers.filterNot(existingHosts apply _.host).traverse(_ (entity)).map(_.flatten)

  private def filterLinksWithNewHosts(existingLinks: BaseLinks[R], newLinks: BaseLinks[R]): BaseLinks[R] = {
    val map = existingLinks.map(_.host).toSet
    newLinks.filterNot(map contains _.host)
  }
}
