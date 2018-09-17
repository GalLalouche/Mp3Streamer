package backend.recon

import backend.storage.OnlineRetrieverCacher
import common.rich.RichT._

import scala.concurrent.ExecutionContext

import scalaz.OptionT
import scalaz.std.scalaFuture._

class ReconcilerCacher[Key <: Reconcilable](repo: ReconStorage[Key], online: Reconciler[Key])
    (implicit ec: ExecutionContext) extends OnlineRetrieverCacher[Key, StoredReconResult](
  localStorage = repo,
  onlineRetriever = online(_).|>(OptionT.apply)
      .map[StoredReconResult](StoredReconResult.unignored)
      .getOrElse(StoredReconResult.NoRecon),
)
