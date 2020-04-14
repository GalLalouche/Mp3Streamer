package backend

import backend.module.TestModuleConfiguration
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.AsyncTestSuite
import org.scalatest.tags.Slow

import scala.concurrent.{ExecutionContext, Future}

import common.storage.Storage
import common.test.{AsyncAuxSpecs, BeforeAndAfterEachAsync}

@Slow
trait StorageSetup extends AsyncAuxSpecs with BeforeAndAfterEachAsync {self: AsyncTestSuite =>
  protected def config: TestModuleConfiguration
  private implicit def ec: ExecutionContext = config.injector.instance[ExecutionContext]
  protected def storage: Storage[_, _]
  private lazy val utils = storage.utils

  override protected def beforeEach(): Future[_] = utils.clearOrCreateTable()
}
