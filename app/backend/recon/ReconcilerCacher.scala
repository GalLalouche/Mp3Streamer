package backend.recon

import common.storage.OnlineRetrieverCacher

import scala.concurrent.ExecutionContext

class ReconcilerCacher[Key <: Reconcilable](repo: ReconStorage[Key], online: OnlineReconciler[Key])
                                           (implicit ec: ExecutionContext)
  extends OnlineRetrieverCacher[Key, (Option[ReconID], Boolean)](repo, online(_).map(_ -> false)) {

}
