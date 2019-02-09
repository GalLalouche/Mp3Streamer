package backend.module

import com.google.inject.Provider
import com.google.inject.matcher.Matchers
import com.google.inject.spi.TypeListener
import common.ModuleUtils
import common.rich.func.ToMoreMonadOps
import common.storage.Storage
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

import scalaz.std.FutureInstances

/** Creates the in-memory tables after creating the storage instances. */
object CleanModule extends ScalaModule with ModuleUtils
    with ToMoreMonadOps with FutureInstances {
  private def storageListener(ecProvider: Provider[ExecutionContext]): TypeListener =
    typeListener[Storage[_, _]](injectee => {
      import common.rich.RichFuture._
      implicit val ec: ExecutionContext = ecProvider.get()
      injectee.utils.createTableIfNotExists.get
    })

  override def configure(): Unit = {
    install(NonPersistentModule)
    bindListener(Matchers.any(), storageListener(provider[ExecutionContext]))
  }
}
