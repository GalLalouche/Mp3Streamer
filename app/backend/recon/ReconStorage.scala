package backend.recon

import common.storage.Storage

import scala.concurrent.Future

trait ReconStorage[Key <: Reconcilable] extends Storage[Key, StoredReconResult] {
  def isIgnored(k: Key): Future[IgnoredReconResult]
}
