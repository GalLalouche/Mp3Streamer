package mains.cover

import com.google.inject.assistedinject.FactoryModuleBuilder
import mains.cover.image.ImageModule
import net.codingwell.scalaguice.ScalaModule

private[mains] object CoverModule extends ScalaModule {
  override def configure(): Unit = {
    install(ImageModule)
    // TODO add to scala guice.
    install(new FactoryModuleBuilder().build(classOf[AsyncFolderImagePanelFactory]))
  }
}
