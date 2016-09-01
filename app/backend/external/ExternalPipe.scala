package backend.external

import backend.Retriever
import backend.external.expansions.ExternalLinkExpander
import backend.external.recons.Reconciler
import backend.recon.{ReconID, Reconcilable}
import common.rich.RichT._
import common.rich.collections.RichSet._
import common.rich.collections.RichTraversableOnce._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Encompasses all online steps for fetching the external link for a given entity.
 * @param reconciler Matches the entity to an API's ID
 * @param linksRetriever Fetches the external links attached to the entity's ID
 * @param expanders Attempts to expand the links found by scraping the initial set
 * @param additionalReconcilers Any additional, standalone reconciliations that can be performed
 */
//TODO recursively deep?
private class ExternalPipe[R <: Reconcilable](reconciler: Retriever[R, ReconID],
                                              linksRetriever: Retriever[ReconID, Links[R]],
                                              expanders: Traversable[ExternalLinkExpander[R]],
                                              additionalReconcilers: Traversable[Reconciler[R]])
                                             (implicit ec: ExecutionContext) extends Retriever[R, Links[R]] {
  private def markDiff(el: ExternalLink[R]): ExternalLink[R] =
    el.copy(host = el.host.copy(name = el.host.name + "*"))
  private def filterLinksWithNewHosts(existingLinks: Links[R], newLinks: Links[R]): Links[R] = {
    val map = existingLinks.map(_.host).toSet
    newLinks.filterNot(map contains _.host)
  }

  // remove expanders that can only returns existing hosts
  private def filterExpanders(existingHosts: Set[Host]) = {
    def compose(es: Traversable[ExternalLinkExpander[R]])(links: Links[R]): Future[Links[R]] = {
      val map = es.mapBy(_.sourceHost)
      Future sequence links.flatMap(e => map.get(e.host).map(_ (e))) map (_.flatten)
    }
    expanders.filterNot(_.potentialHostsExtracted.toSet <= existingHosts) |> compose
  }

  private def applyFilteredReconcilers(r: R, existingHosts: Set[Host]) = {
    def filterReconcilers(existingHosts: Set[Host]) = // removes reconcilers for existing hosts
      additionalReconcilers.filterNot(_.host |> existingHosts)
    Future.sequence(filterReconcilers(existingHosts).map(_ (r))).map(_.flatten)
  }

  private def expand(r: R, existing: Links[R]): Future[Links[R]] = {
    def extractHosts(ls: Links[R]) = ls.map(_.host).toSet
    val existingHosts = existing |> extractHosts
    for (newLinks <- filterExpanders(existingHosts)(existing);
         additionalLinks <- applyFilteredReconcilers(r, existingHosts ++ extractHosts(newLinks))) yield {
      val existingSet = existing.toSet
      val newSet = newLinks.++(additionalLinks).toSet
      existingSet ++ (filterLinksWithNewHosts(existingSet, newSet) map markDiff)
    }
  }
  override def apply(r: R): Future[Links[R]] =
    reconciler(r)
        .flatMap(linksRetriever)
        .flatMap(expand(r, _))
}
