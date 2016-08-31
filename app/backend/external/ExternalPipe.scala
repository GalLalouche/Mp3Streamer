package backend.external

import backend.Retriever
import backend.external.expansions.ExternalLinkExpander
import backend.recon.{ReconID, Reconcilable}
import common.rich.RichT._
import common.rich.collections.RichSet._
import common.rich.collections.RichTraversableOnce._

import scala.concurrent.{ExecutionContext, Future}

class ExternalPipe[R <: Reconcilable](reconciler: Retriever[R, ReconID],
                                      provider: Retriever[ReconID, Links[R]],
                                      expander: Traversable[ExternalLinkExpander[R]],
                                      additionalReconciler: Traversable[Reconciler[R]])
                                     (implicit ec: ExecutionContext) extends Retriever[R, Links[R]] {
  private def markDiff(el: ExternalLink[R]): ExternalLink[R] =
    el.copy(host = el.host.copy(name = el.host.name + "*"))
  private def getDiff(existingLinks: Links[R], newLinks: Links[R]): Links[R] = {
    val map = existingLinks.map(_.host).toSet
    newLinks.filterNot(map contains _.host)
  }

  private def filterExpanders(existingHosts: Set[Host]) = {
    def compose(es: Traversable[ExternalLinkExpander[R]]): Retriever[Links[R], Links[R]] = links => {
      val map = es.mapBy(_.sourceHost)
      Future sequence links.flatMap(e => map.get(e.host).map(_ (e))) map (_.flatten)
    }
    expander.filterNot(e => existingHosts >= e.potentialHostsExtracted.toSet) |> compose
  }
  private def filterReconcilers(existingHosts: Set[Host]) =
    additionalReconciler.filterNot(_.host |> existingHosts)
  private def expand(r: R, existing: Links[R]): Future[Links[R]] = {
    val existingHosts = existing.map(_.host).toSet
    for (newLinks <- filterExpanders(existingHosts).apply(existing);
         additionalLinks <- Future sequence filterReconcilers(existingHosts).map(_ (r)) map (_.flatten)) yield {
      val existingSet: Set[ExternalLink[R]] = existing.toSet
      val newSet = newLinks.++(additionalLinks).toSet
      existingSet ++ (getDiff(existingSet, newSet) map markDiff)
    }
  }
  override def apply(r: R): Future[Links[R]] =
    reconciler(r)
        .flatMap(provider)
        .flatMap(expand(r, _))
}
object ExternalPipe {
}
