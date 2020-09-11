package backend.recon

import backend.recon.StoredReconResult.{HasReconResult, NoRecon}
import backend.FutureOption

import scala.concurrent.Future

import common.storage.Storage

trait ReconStorage[R <: Reconcilable] extends Storage[R, StoredReconResult] {
  // TODO move to a different table
  def isIgnored(k: R): Future[IgnoredReconResult]
  def update(key: R, reconId: ReconID): FutureOption[StoredReconResult] = mapStore(key, {
    case NoRecon => StoredReconResult.unignored(reconId)
    case HasReconResult(_, isIgnored) => HasReconResult(reconId, isIgnored)
  }, default = StoredReconResult.unignored(reconId))
}

trait ArtistReconStorage extends ReconStorage[Artist]
trait AlbumReconStorage extends ReconStorage[Album]
