package backend.external

import java.time.{Clock, Duration}

import backend.Retriever
import backend.external.expansions.ExternalLinkExpander
import backend.external.mark.ExternalLinkMarker
import backend.external.recons.LinkRetrievers
import backend.recon.{Reconcilable, ReconcilerCacher, ReconID}
import backend.recon.StoredReconResult.{HasReconResult, StoredNull}
import backend.storage.{ComposedFreshnessStorage, RefreshableRetriever}
import com.google.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreMonadErrorOps._
import scalaz.{-\/, \/-}

import common.rich.RichT._

private class ExternalPipeWrapper[R <: Reconcilable] @Inject() (
    clock: Clock,
    ec: ExecutionContext,
    reconciler: ReconcilerCacher[R],
    storage: ExternalStorage[R],
    provider: Retriever[ReconID, BaseLinks[R]],
    expanders: Traversable[ExternalLinkExpander[R]],
    markers: Traversable[ExternalLinkMarker[R]],
) {
  private implicit val iec: ExecutionContext = ec

  def apply(
      standaloneReconcilers: LinkRetrievers[R],
  ): Retriever[R, TimestampedLinks[R]] = new RefreshableRetriever[R, MarkedLinks[R]](
    freshnessStorage = new ComposedFreshnessStorage(storage, clock),
    onlineRetriever = new ExternalPipe[R](
      r =>
        reconciler(r).mapEitherMessage {
          case Failure(e) => -\/(s"Failed to retrieve recon ID from online source for <$r>")
          case Success(StoredNull) =>
            -\/(s"Storage contained null for <$r>, i.e., past recon attempts failed")
          case Success(HasReconResult(reconId, _)) => \/-(reconId)
        },
      provider,
      standaloneReconcilers,
      expanders,
      markers,
    ),
    maxAge = Duration.ofDays(365),
    clock = clock,
  ).|>(new ExternalPipeWrapper.TimeStamper(_))
}

private object ExternalPipeWrapper {
  private class TimeStamper[R <: Reconcilable](storage: RefreshableRetriever[R, MarkedLinks[R]])(
      implicit ec: ExecutionContext,
  ) extends Retriever[R, TimestampedLinks[R]] {
    override def apply(r: R): Future[TimestampedLinks[R]] =
      storage.withAge(r).map(e => TimestampedLinks(e._1, e._2.localDateTime.get))
  }
}
