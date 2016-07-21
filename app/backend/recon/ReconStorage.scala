package backend.recon

import common.storage.NewLocalStorage

trait ReconStorage[Key <: Reconcilable] extends NewLocalStorage[Key, (Option[ReconID], Boolean)] {
  protected def normalize(k: Key): String
}
