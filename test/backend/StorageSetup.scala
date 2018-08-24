package backend

import backend.configs.TestConfiguration
import common.rich.RichFuture._
import common.storage.Storage
import org.scalatest.tags.Slow
import org.scalatest.{BeforeAndAfter, OneInstancePerTest, Suite}

@Slow
trait StorageSetup extends BeforeAndAfter with OneInstancePerTest {self: Suite =>
  protected implicit def config: TestConfiguration
  protected def storage: Storage[_, _]
  private lazy val utils = storage.utils
  before(utils.createTable().get)
}
