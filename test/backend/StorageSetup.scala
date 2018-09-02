package backend

import backend.module.TestModuleConfiguration
import common.rich.RichFuture._
import common.storage.Storage
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.{BeforeAndAfter, OneInstancePerTest, Suite}
import org.scalatest.tags.Slow

import scala.concurrent.ExecutionContext

@Slow
trait StorageSetup extends BeforeAndAfter with OneInstancePerTest {self: Suite =>
  protected def config: TestModuleConfiguration
  private implicit def ec: ExecutionContext = config.injector.instance[ExecutionContext]
  protected def storage: Storage[_, _]
  private lazy val utils = storage.utils
  before(utils.createTable().get)
}
