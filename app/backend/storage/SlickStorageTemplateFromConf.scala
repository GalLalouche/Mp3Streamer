package backend.storage

import scala.concurrent.ExecutionContext

import common.storage.SlickStorageTemplate

abstract class SlickStorageTemplateFromConf[Key, Value](ec: ExecutionContext, dbP: DbProvider)
    extends SlickStorageTemplate[Key, Value]()(ec) {
  protected override val profile = dbP.profile
  protected override val db = dbP.db
}
