package mains.albums

import common.storage.LocalStorage

trait ReconStorage extends LocalStorage[String, Option[String]] {
  protected def normalize(s: String): String = s.toLowerCase
}
