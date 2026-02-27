package backend.module

import com.google.inject.matcher.Matchers
import net.codingwell.scalaguice.ScalaModule

import scala.util.Try

import common.guice.ModuleUtils
import common.storage.Storage

/** Creates tables after creating the storage instances. */
object StorageAutoCreateModule extends ScalaModule with ModuleUtils {
  override def configure(): Unit =
    bindListener(Matchers.any(), typeListener[Storage[_, _]] { injectee =>
      import common.rich.RichFuture._
      // Some storages share constraint names (e.g., "artist_fk") without using
      // constraintMangler, causing "already exists" errors in H2. Using Try
      // ignores these harmless collisions since the table itself is still created.
      Try(injectee.utils.createTableIfNotExists().get)
    })
}
