package backend

import backend.module.TestModuleConfiguration
import common.storage.Storage
import common.test.{AsyncAuxSpecs, BeforeAndAfterEachAsync}
import org.scalatest.AsyncTestSuite
import org.scalatest.tags.Slow
import scala.concurrent.Future

@Slow
trait StorageSetup extends AsyncAuxSpecs with BeforeAndAfterEachAsync { self: AsyncTestSuite =>
  protected def config: TestModuleConfiguration
  protected def storage: Storage[_, _]
  private lazy val utils = storage.utils

  protected override def beforeEach(): Future[_] = utils.clearOrCreateTable()
}
