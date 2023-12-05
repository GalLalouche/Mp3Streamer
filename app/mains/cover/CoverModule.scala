package mains.cover

import com.google.inject.assistedinject.FactoryModuleBuilder
import common.io.google.GoogleModule
import net.codingwell.scalaguice.ScalaModule

private[mains] object CoverModule extends ScalaModule {
  override def configure(): Unit = {
    install(GoogleModule)
    // TODO add to scala guice.
    install(new FactoryModuleBuilder().build(classOf[AsyncFolderImagePanelFactory]))
  }
}
