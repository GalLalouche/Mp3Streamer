package backend.module

import com.google.inject.{Provider, TypeLiteral}
import com.google.inject.matcher.Matchers
import com.google.inject.spi.{InjectionListener, TypeEncounter, TypeListener}
import common.ModuleUtils
import common.storage.Storage
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

/** Also creates all needed tables after creating the storage instances them. */
object CleanModule extends ScalaModule with ModuleUtils {
  // TODO extract to module utils
  private def storageListener(ecProvider: Provider[ExecutionContext]): TypeListener =
    new TypeListener with InjectionListener[Storage[_, _]] {
      import common.rich.RichFuture._

      override def hear[I](`type`: TypeLiteral[I], encounter: TypeEncounter[I]): Unit = {
        if (`type`.getRawType.isAssignableFrom(classOf[Storage[_, _]]))
          encounter.register(this.asInstanceOf[InjectionListener[I]])
      }
      override def afterInjection(injectee: Storage[_, _]) = {
        implicit val ec: ExecutionContext = ecProvider.get()
        injectee.utils.createTable().get
      }
    }

  override def configure(): Unit = {
    install(NonPersistentModule)
    bindListener(Matchers.any(), storageListener(provider[ExecutionContext]))
  }
}
