package backend.recon

import backend.storage.Storage

import scala.concurrent.Future

trait ReconStorage[Key <: Reconcilable] extends Storage[Key, (Option[ReconID], Boolean)] {
  def isIgnored(k: Key): Future[Option[Boolean]]
}
