package backend.recon

import common.storage.LocalStorage

trait ReconStorage[Key] extends LocalStorage[Key, (Option[ReconID], Boolean)] {
  protected def normalize(s: String): String = s.toLowerCase
}
