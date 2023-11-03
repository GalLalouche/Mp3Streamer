package backend

import scala.concurrent.{ExecutionContext, Future}

import org.scalatest.tags.Slow
import org.scalatest.AsyncTestSuite

import backend.module.TestModuleConfiguration
import common.storage.Storage
import common.test.{AsyncAuxSpecs, BeforeAndAfterEachAsync}
import net.codingwell.scalaguice.InjectorExtensions._

@Slow
trait StorageSetup extends AsyncAuxSpecs with BeforeAndAfterEachAsync { self: AsyncTestSuite =>
  protected def config: TestModuleConfiguration
  private implicit def ec: ExecutionContext = config.injector.instance[ExecutionContext]
  protected def storage: Storage[_, _]
  private lazy val utils = storage.utils

  protected override def beforeEach(): Future[_] = utils.clearOrCreateTable()
}
