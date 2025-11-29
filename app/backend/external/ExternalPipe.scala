package backend.external

import alleycats.std.iterable.alleycatsStdIterableTraverse
import backend.Retriever
import backend.external.expansions.ExternalLinkExpander
import backend.external.mark.ExternalLinkMarker
import backend.external.recons.LinkRetrievers
import backend.recon.{Reconcilable, ReconID}

import scala.concurrent.{ExecutionContext, Future}

import cats.syntax.flatMap.catsSyntaxFlatMapOps
import cats.syntax.traverse.toTraverseOps
import common.rich.func.kats.MoreIterableInstances.iterableInstances
import common.rich.func.kats.ToMoreFoldableOps._

import common.rich.RichTuple.richTuple2
import common.rich.collections.RichSet._
import common.rich.collections.RichTraversableOnce._
import common.rich.primitives.RichOption.richOption

/**
 * Encompasses all online steps for fetching the external links for a given entity.
 *
 * @param reconciler
 *   Matches the entity to an API's ID
 * @param linksRetriever
 *   Fetches the external links attached to the entity's ID
 * @param standaloneReconcilers
 *   Any additional, standalone reconciliations that can be performed
 * @param expanders
 *   Attempts to expand the links found by scraping the initial set
 * @param markers
 *   Adds special markers to the links, e.g., new or missing
 */
private class ExternalPipe[R <: Reconcilable](
    reconciler: Retriever[R, ReconID],
    linksRetriever: Retriever[ReconID, BaseLinks[R]],
    standaloneReconcilers: LinkRetrievers[R],
    expanders: Iterable[ExternalLinkExpander[R]],
    markers: Iterable[ExternalLinkMarker[R]],
)(implicit ec: ExecutionContext)
    extends Retriever[R, MarkedLinks[R]] {
  override def apply(r: R): Future[MarkedLinks[R]] =
    reconciler(r) >>= linksRetriever >>= expand(r) >>= mark

  private def expand(r: R)(existingLinks: BaseLinks[R]): Future[MarkedLinks[R]] =
    for {
      reconciledLinks <- applyNewHostReconcilers(r, extractHosts(existingLinks))
      newLinks <- applyNewHostExpanders(existingLinks ++ reconciledLinks)
    } yield {
      val existingSet = existingLinks.toSet
      val newSet = newLinks.toSet ++ reconciledLinks
      existingSet.map(MarkedLink.markExisting) ++
        filterLinksWithNewHosts(existingSet, newSet).map(MarkedLink.markNew)
    }

  private def extractHosts(ls: BaseLinks[R]) = ls.map(_.host).toSet

  private def applyNewHostReconcilers(entity: R, existingHosts: Set[Host]): Future[BaseLinks[R]] =
    standaloneReconcilers.get
      .filterNot(existingHosts apply _.host)
      .traverse(_(entity).value)
      .map(_.flatten)

  private def filterLinksWithNewHosts(
      existingLinks: BaseLinks[R],
      newLinks: BaseLinks[R],
  ): BaseLinks[R] = {
    val map = existingLinks.map(_.host).toSet
    newLinks.filterNot(map contains _.host)
  }

  private def applyNewHostExpanders(links: BaseLinks[R]): Future[BaseLinks[R]] = {
    def aux(result: Set[BaseLink[R]]): Future[Set[BaseLink[R]]] = {
      val existingHosts = extractHosts(links ++ result)
      val nextExpandersByHost =
        expanders
          .filterNot(_.potentialHostsExtracted.toSet <= existingHosts)
          .toMultiMap(_.sourceHost)
      val expanderLinkPairs: Iterable[(ExternalLinkExpander[R], BaseLink[R])] = for {
        r <- result
        expanders <- nextExpandersByHost.get(r.host).asIterable
        expander <- expanders
      } yield expander -> r
      for {
        newLinkSet <- expanderLinkPairs.flatTraverse(_.reduce(_ expand _)).map(_.toSet)
        noNewLinks = newLinkSet <= result
        r <- if (noNewLinks) Future.successful(result) else aux(newLinkSet ++ result)
      } yield r
    }
    aux(links.toSet)
  }

  private def mark(links: Iterable[MarkedLink[R]]): Future[MarkedLinks[R]] = links.traverse(l =>
    markers
      .filter(_.host == l.host)
      .singleOpt
      .mapHeadOrElse(_(l).map(m => l.copy(mark = m)), Future.successful(l)),
  )
}
