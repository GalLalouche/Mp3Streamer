package backend.configs

import backend.mb.MbModule
import net.codingwell.scalaguice.ScalaModule

object AllModules extends ScalaModule {
  override def configure(): Unit = {
    install(MbModule)
  }
}
