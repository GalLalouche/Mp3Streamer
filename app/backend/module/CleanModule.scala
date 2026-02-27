package backend.module

import net.codingwell.scalaguice.ScalaModule

/** Creates the in-memory tables after creating the storage instances. */
object CleanModule extends ScalaModule {
  override def configure(): Unit = {
    install(NonPersistentModule)
    install(StorageAutoCreateModule)
  }
}
