package backend.storage

import backend.configs.Configuration
import common.storage.SlickStorageTemplate
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.ExecutionContext

abstract class SlickStorageTemplateFromConf[Key, Value](implicit c: Configuration, ec: ExecutionContext)
    extends SlickStorageTemplate[Key, Value] {
  private val dbP = c.injector.instance[DbProvider]
  override protected val profile = dbP.profile
  override protected val db = dbP.db
}
