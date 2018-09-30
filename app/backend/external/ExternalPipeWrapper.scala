package backend.external

import java.time.{Clock, Duration}

import backend.recon.{Reconcilable, ReconID, StoredReconResult}
import backend.Retriever
import backend.external.expansions.ExternalLinkExpander
import backend.external.recons.LinkRetrievers
import backend.recon.StoredReconResult.{HasReconResult, NoRecon}
import backend.storage.{FreshnessStorage, RefreshableStorage}
import common.rich.RichT._
import common.rich.func.ToMoreMonadErrorOps
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import scalaz.{-\/, \/-}
import scalaz.std.FutureInstances

private class ExternalPipeWrapper[R <: Reconcilable] @Inject()(
    clock: Clock,
    ec: ExecutionContext,
    storage: ExternalStorage[R],
    provider: Retriever[ReconID, BaseLinks[R]],
    expanders: Traversable[ExternalLinkExpander[R]],
) extends ToMoreMonadErrorOps with FutureInstances {
  private implicit val iec: ExecutionContext = ec

  def apply(
      reconciler: Retriever[R, StoredReconResult],
      standaloneReconcilers: LinkRetrievers[R],
  ): Retriever[R, TimestampedLinks[R]] = new RefreshableStorage[R, MarkedLinks[R]](
    freshnessStorage = new FreshnessStorage(storage, clock),
    onlineRetriever = new ExternalPipe[R](
      r => reconciler(r).mapEitherMessage({
        case NoRecon => -\/(s"Couldn't reconcile <$r>")
        case HasReconResult(reconId, _) => \/-(reconId)
      }),
      provider,
      standaloneReconcilers,
      expanders,
    ),
    maxAge = Duration ofDays 28,
    clock = clock,
  ).mapTo(new ExternalPipeWrapper.TimeStamper(_))
}

private object ExternalPipeWrapper {
  private class TimeStamper[R <: Reconcilable](
      storage: RefreshableStorage[R, MarkedLinks[R]])(
      implicit ec: ExecutionContext)
      extends Retriever[R, TimestampedLinks[R]] {
    override def apply(r: R): Future[TimestampedLinks[R]] =
      storage.withAge(r).map(e => TimestampedLinks(e._1, e._2.localDateTime.get))
  }
}
