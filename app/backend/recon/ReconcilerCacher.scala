package backend.recon

import backend.storage.OnlineRetrieverCacher

import scala.concurrent.ExecutionContext

class ReconcilerCacher[Key <: Reconcilable](repo: ReconStorage[Key], online: Reconciler[Key])
                                           (implicit ec: ExecutionContext)
  extends OnlineRetrieverCacher[Key, (Option[ReconID], Boolean)](repo, online(_).map(_ -> false))
