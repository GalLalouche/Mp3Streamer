package backend

import backend.module.TestModuleConfiguration
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.{AsyncTestSuite, OneInstancePerTest}
import org.scalatest.tags.Slow

import scala.concurrent.ExecutionContext

import common.storage.Storage
import common.AsyncAuxSpecs

@Slow
trait StorageSetup extends OneInstancePerTest with AsyncAuxSpecs {self: AsyncTestSuite =>
  protected def config: TestModuleConfiguration
  private implicit def ec: ExecutionContext = config.injector.instance[ExecutionContext]
  protected def storage: Storage[_, _]
  private lazy val utils = storage.utils

  override def withFixture(test: NoArgAsyncTest) = runBefore(utils.createTable())(test)
}
