package backend.pkg

import com.google.inject.assistedinject.FactoryModuleBuilder
import net.codingwell.scalaguice.ScalaModule

private object PkgModule extends ScalaModule {
  override def configure(): Unit = {
    install(new FactoryModuleBuilder().build(classOf[ZipperFactory]))
  }
}
