package backend.storage

import scala.concurrent.ExecutionContext

import common.storage.SlickSingleKeyColumnStorageTemplate

abstract class SlickSingleKeyColumnStorageTemplateFromConf[Key, Value](
    ec: ExecutionContext,
    dbP: DbProvider,
) extends SlickSingleKeyColumnStorageTemplate[Key, Value]()(ec) {
  protected override val profile = dbP.profile
  protected override val db = dbP.db
}
