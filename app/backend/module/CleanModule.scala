package backend.module

import scala.concurrent.ExecutionContext

import com.google.inject.matcher.Matchers
import com.google.inject.spi.TypeListener
import com.google.inject.Provider
import common.guice.ModuleUtils
import common.storage.Storage
import net.codingwell.scalaguice.ScalaModule

/** Creates the in-memory tables after creating the storage instances. */
object CleanModule extends ScalaModule with ModuleUtils {
  private def storageListener(ecProvider: Provider[ExecutionContext]): TypeListener =
    typeListener[Storage[_, _]] { injectee =>
      import common.rich.RichFuture._
      implicit val ec: ExecutionContext = ecProvider.get()
      injectee.utils.createTableIfNotExists.get
    }

  override def configure(): Unit = {
    install(NonPersistentModule)
    bindListener(Matchers.any(), storageListener(provider[ExecutionContext]))
  }
}
