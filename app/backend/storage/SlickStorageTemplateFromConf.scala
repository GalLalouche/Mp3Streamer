package backend.storage

import common.storage.SlickStorageTemplate

import scala.concurrent.ExecutionContext

abstract class SlickStorageTemplateFromConf[Key, Value](ec: ExecutionContext, dbP: DbProvider)
    extends SlickStorageTemplate[Key, Value]()(ec) {
  override protected val profile = dbP.profile
  override protected val db = dbP.db
}
