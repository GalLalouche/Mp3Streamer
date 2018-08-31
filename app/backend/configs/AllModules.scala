package backend.configs

import backend.mb.MbModule
import net.codingwell.scalaguice.ScalaModule
import songs.SongsModule

object AllModules extends ScalaModule {
  override def configure(): Unit = {
    install(MbModule)
    install(SongsModule)
  }
}
