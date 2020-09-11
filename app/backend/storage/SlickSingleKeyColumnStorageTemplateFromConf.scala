package backend.storage

import scala.concurrent.ExecutionContext

import common.storage.SlickSingleKeyColumnStorageTemplate

abstract class SlickSingleKeyColumnStorageTemplateFromConf[Key, Value](ec: ExecutionContext, dbP: DbProvider)
    extends SlickSingleKeyColumnStorageTemplate[Key, Value]()(ec) {
  override protected val profile = dbP.profile
  override protected val db = dbP.db
}
