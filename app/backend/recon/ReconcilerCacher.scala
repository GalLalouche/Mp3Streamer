package backend.recon

import backend.storage.OnlineRetrieverCacher

import scala.concurrent.ExecutionContext

import common.rich.func.kats.RichOptionT.richOptionT

class ReconcilerCacher[Key <: Reconcilable](repo: ReconStorage[Key], online: Reconciler[Key])(
    implicit ec: ExecutionContext,
) extends OnlineRetrieverCacher[Key, StoredReconResult](
      localStorage = repo,
      onlineRetriever =
        online(_).map[StoredReconResult](StoredReconResult.unignored) | StoredReconResult.StoredNull,
    )
