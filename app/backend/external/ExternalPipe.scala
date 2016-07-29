package backend.external

import backend.recon.{ReconID, Reconcilable}
import backend.storage.Retriever

import scala.concurrent.{ExecutionContext, Future}

class ExternalPipe[T <: Reconcilable](reconciler: Retriever[T, ReconID],
                                      provider: Retriever[ReconID, Links[T]],
                                      expander: Retriever[Links[T], Links[T]])
                                     (implicit ec: ExecutionContext) extends Retriever[T, Links[T]] {
  private def expand(links: Links[T]) = expander(links).map(_.toSet ++ links.toSet)
  override def apply(t: T): Future[Links[T]] =
    reconciler(t)
        .flatMap(provider)
        .flatMap(expand)
}
