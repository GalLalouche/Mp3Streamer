package backend.module

import com.google.inject.{Provider, TypeLiteral}
import com.google.inject.matcher.Matchers
import com.google.inject.spi.{InjectionListener, TypeEncounter, TypeListener}
import common.rich.primitives.RichClass._
import common.ModuleUtils
import common.rich.func.ToMoreMonadOps
import common.storage.Storage
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

import scalaz.std.FutureInstances

/** Also creates all needed tables after creating the storage instances them. */
object CleanModule extends ScalaModule with ModuleUtils
    with ToMoreMonadOps with FutureInstances {
  // TODO extract to module utils
  private def storageListener(ecProvider: Provider[ExecutionContext]): TypeListener =
    new TypeListener with InjectionListener[Storage[_, _]] {
      import common.rich.RichFuture._

      override def hear[I](`type`: TypeLiteral[I], encounter: TypeEncounter[I]): Unit = {
        if (`type`.getRawType.isAssignableTo(classOf[Storage[_, _]]))
          encounter.register(this.asInstanceOf[InjectionListener[I]])
      }
      override def afterInjection(injectee: Storage[_, _]) = {
        implicit val ec: ExecutionContext = ecProvider.get()
        // TODO createTableIfItDoesn'tExist?
        injectee.utils.doesTableExist.ifFalse(injectee.utils.createTable).get
      }
    }

  override def configure(): Unit = {
    install(NonPersistentModule)
    bindListener(Matchers.any(), storageListener(provider[ExecutionContext]))
  }
}
