package backend.external

import backend.Retriever
import backend.external.expansions.ExternalLinkExpander
import backend.external.recons.Reconciler
import backend.recon.{ReconID, Reconcilable}
import common.rich.RichT._
import common.rich.collections.RichSet._
import common.rich.collections.RichTraversableOnce._
import common.rich.func.MoreTraverseInstances

import scala.concurrent.{ExecutionContext, Future}
import scalaz.std.FutureInstances
import scalaz.syntax.ToTraverseOps

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
    with ToTraverseOps with FutureInstances with MoreTraverseInstances {
  private def filterLinksWithNewHosts(existingLinks: BaseLinks[R], newLinks: BaseLinks[R]): BaseLinks[R] = {
    val map = existingLinks.map(_.host).toSet
    newLinks.filterNot(map contains _.host)
  }

  // remove expanders that can only returns existing hosts
  private def filterExpanders(existingHosts: Set[Host]) = {
    def compose(es: Traversable[ExternalLinkExpander[R]])(links: BaseLinks[R]): Future[BaseLinks[R]] = {
      val map = es.mapBy(_.sourceHost)
      links.flatMap(e => map.get(e.host).map(_ (e))).sequenceU map (_.flatten)
    }
    expanders.filterNot(_.potentialHostsExtracted.toSet <= existingHosts) |> compose
  }

  private def applyFilteredReconcilers(r: R, existingHosts: Set[Host]) = {
    def filterReconcilers(existingHosts: Set[Host]) = // removes reconcilers for existing hosts
      additionalReconcilers.filterNot(_.host |> existingHosts)
    filterReconcilers(existingHosts).traverse(_ (r)).map(_.flatten)
  }

  private def expand(r: R, existing: BaseLinks[R]): Future[MarkedLinks[R]] = {
    def extractHosts(ls: BaseLinks[R]) = ls.map(_.host).toSet
    val existingHosts = existing |> extractHosts
    for {
      newLinks <- filterExpanders(existingHosts)(existing)
      additionalLinks <- applyFilteredReconcilers(r, existingHosts ++ extractHosts(newLinks))
    } yield {
      val existingSet = existing.toSet
      val newSet = newLinks.++(additionalLinks).toSet
      existingSet.map(MarkedLink.markExisting) ++
          filterLinksWithNewHosts(existingSet, newSet).map(MarkedLink.markNew)
    }
  }

  override def apply(r: R): Future[MarkedLinks[R]] =
    reconciler(r)
        .flatMap(linksRetriever)
        .flatMap(expand(r, _))
}
