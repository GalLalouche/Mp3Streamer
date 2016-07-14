package backend.recon

import common.storage.LocalStorage

trait ReconStorage extends LocalStorage[String, (Option[String], Boolean)] {
  protected def normalize(s: String): String = s.toLowerCase
}
