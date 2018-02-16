package backend.storage

import common.storage.SlickStorageTemplate

abstract class SlickStorageTemplateFromConf[Key, Value](implicit protected val dbP: DbProvider)
    extends SlickStorageTemplate[Key, Value] {
  override protected val profile = dbP.profile
  override protected val db = dbP.db
}
