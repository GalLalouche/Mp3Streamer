package mains.cover

import com.google.inject.assistedinject.FactoryModuleBuilder
import net.codingwell.scalaguice.ScalaModule

import common.io.google.GoogleModule

private[mains] object CoverModule extends ScalaModule {
  override def configure(): Unit = {
    install(GoogleModule)
    // TODO add to scala guice.
    install(new FactoryModuleBuilder().build(classOf[AsyncFolderImagePanelFactory]))
  }
}
