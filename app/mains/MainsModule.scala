package mains

import backend.module.StandaloneModule
import mains.cover.CoverModule
import net.codingwell.scalaguice.ScalaModule

private object MainsModule extends ScalaModule {
  override def configure(): Unit = {
    install(CoverModule)
    install(StandaloneModule)
  }
}
