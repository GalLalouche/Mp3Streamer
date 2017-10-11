package backend.storage

import backend.configs.Configuration
import common.storage.SlickStorageTemplate

abstract class SlickStorageTemplateFromConf[Key, Value](implicit protected val c: Configuration)
    extends SlickStorageTemplate[Key, Value] {
  override protected val profile = c.profile
  override protected val db = c.db
}
