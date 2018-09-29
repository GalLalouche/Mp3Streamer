package backend.recon

import backend.recon.StoredReconResult.{HasReconResult, NoRecon}
import common.storage.Storage

import scala.concurrent.Future

trait ReconStorage[R <: Reconcilable] extends Storage[R, StoredReconResult] {
  def isIgnored(k: R): Future[IgnoredReconResult]
  def update(key: R, reconId: ReconID): Future[_] = mapStore(key, {
    case NoRecon => StoredReconResult.unignored(reconId)
    case HasReconResult(_, isIgnored) => HasReconResult(reconId, isIgnored)
  }, default = StoredReconResult.unignored(reconId))
}
