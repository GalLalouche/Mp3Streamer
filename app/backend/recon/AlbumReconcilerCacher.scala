package backend.recon

import common.storage.OnlineRetrieverCacher

import scala.concurrent.ExecutionContext

class AlbumReconcilerCacher(repo: ReconStorage[Album], online: OnlineReconciler[Album])
                           (implicit ec: ExecutionContext)
  extends OnlineRetrieverCacher[Album, (Option[ReconID], Boolean)](repo, online(_).map(_ -> false))

