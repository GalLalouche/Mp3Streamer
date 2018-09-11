package backend.recon

import backend.storage.OnlineRetrieverCacher

import scala.concurrent.ExecutionContext

class ReconcilerCacher[Key <: Reconcilable](repo: ReconStorage[Key], online: Reconciler[Key])
    (implicit ec: ExecutionContext) extends OnlineRetrieverCacher[Key, StoredReconResult](
  localStorage = repo,
  onlineRetriever = online(_).map(_.map(StoredReconResult.unignored).getOrElse(StoredReconResult.NoRecon)),
)
