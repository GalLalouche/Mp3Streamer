package backend

import backend.configs.TestConfiguration
import common.rich.RichFuture._
import common.storage.Storage
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Suite}

trait StorageSetup extends BeforeAndAfterAll with BeforeAndAfter {self: Suite =>
  protected implicit def config: TestConfiguration
  protected def storage: Storage[_, _]
  private lazy val utils = storage.utils
  override def beforeAll = {
    utils.createTable().get
  }
  after {
    utils.clearTable().get
  }
}
