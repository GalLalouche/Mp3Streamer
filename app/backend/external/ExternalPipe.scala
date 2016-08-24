package backend.external

import backend.recon.{ReconID, Reconcilable}
import backend.storage.Retriever

import scala.concurrent.{ExecutionContext, Future}

class ExternalPipe[T <: Reconcilable](reconciler: Retriever[T, ReconID],
                                      provider: Retriever[ReconID, Links[T]],
                                      expander: Retriever[Links[T], Links[T]])
                                     (implicit ec: ExecutionContext) extends Retriever[T, Links[T]] {
  private def markDiff(el: ExternalLink[T]): ExternalLink[T] =
    el.copy(host = el.host.copy(name = el.host.name + "*"))
  private def getDiff(existingLinks: Links[T], newLinks: Links[T]): Links[T] = {
    val map = existingLinks.map(_.host).toSet
    newLinks.filterNot(map contains _.host)
  }
  private def expand(existing: Links[T]): Future[Links[T]] = expander(existing).map { newLinks =>
    val existingSet: Set[ExternalLink[T]] = existing.toSet
    existingSet ++ (getDiff(existingSet, newLinks) map markDiff)
  }
  override def apply(t: T): Future[Links[T]] =
    reconciler(t)
        .flatMap(provider)
        .flatMap(expand)
}
