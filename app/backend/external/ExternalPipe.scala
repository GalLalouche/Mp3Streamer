package backend.external

import backend.Retriever
import backend.recon.{ReconID, Reconcilable}

import scala.concurrent.{ExecutionContext, Future}

class ExternalPipe[R <: Reconcilable](reconciler: Retriever[R, ReconID],
                                      provider: Retriever[ReconID, Links[R]],
                                      expander: Retriever[Links[R], Links[R]],
                                      additionalReconciler: Retriever[R, Links[R]])
                                     (implicit ec: ExecutionContext) extends Retriever[R, Links[R]] {
  private def markDiff(el: ExternalLink[R]): ExternalLink[R] =
    el.copy(host = el.host.copy(name = el.host.name + "*"))
  private def getDiff(existingLinks: Links[R], newLinks: Links[R]): Links[R] = {
    val map = existingLinks.map(_.host).toSet
    newLinks.filterNot(map contains _.host)
  }
  private def expand(r: R, existing: Links[R]): Future[Links[R]] =
    for (newLinks <- expander(existing); additionalLinks <- additionalReconciler(r)) yield {
      val existingSet: Set[ExternalLink[R]] = existing.toSet
      val newSet = newLinks.++(additionalLinks).toSet
      existingSet ++ (getDiff(existingSet, newSet) map markDiff)
    }
  override def apply(r: R): Future[Links[R]] =
    reconciler(r)
        .flatMap(provider)
        .flatMap(expand(r, _))
}
