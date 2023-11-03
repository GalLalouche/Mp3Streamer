package backend.recon

import scala.concurrent.ExecutionContext
import scalaz.std.scalaFuture._

import backend.storage.OnlineRetrieverCacher

class ReconcilerCacher[Key <: Reconcilable](repo: ReconStorage[Key], online: Reconciler[Key])(
    implicit ec: ExecutionContext,
) extends OnlineRetrieverCacher[Key, StoredReconResult](
      localStorage = repo,
      onlineRetriever =
        online(_).map[StoredReconResult](StoredReconResult.unignored) | StoredReconResult.NoRecon,
    )
