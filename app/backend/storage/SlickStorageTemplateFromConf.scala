package backend.storage

import scala.concurrent.ExecutionContext

import common.storage.{SlickSingleKeyColumnStorageTemplate, SlickStorageTemplate}

abstract class SlickStorageTemplateFromConf[Key, Value](ec: ExecutionContext, dbP: DbProvider)
    extends SlickStorageTemplate[Key, Value]()(ec) {
  override protected val profile = dbP.profile
  override protected val db = dbP.db
}
