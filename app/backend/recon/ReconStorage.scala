package backend.recon

import backend.recon.StoredReconResult.{HasReconResult, NoRecon}
import backend.FutureOption
import backend.storage.CachableStorage

import scala.concurrent.Future

import common.storage.{StorageTemplate, StoreMode}

trait ReconStorage[R <: Reconcilable]
    extends StorageTemplate[R, StoredReconResult]
        with CachableStorage[R, StoredReconResult] {
  // TODO move to a different table
  def isIgnored(k: R): Future[IgnoredReconResult]
  def update(key: R, reconId: ReconID): FutureOption[StoredReconResult] = mapStore(StoreMode.Update, key, {
    case NoRecon => StoredReconResult.unignored(reconId)
    case HasReconResult(_, isIgnored) => HasReconResult(reconId, isIgnored)
  }, default = StoredReconResult.unignored(reconId))
}

trait ArtistReconStorage extends ReconStorage[Artist]
trait AlbumReconStorage extends ReconStorage[Album]
