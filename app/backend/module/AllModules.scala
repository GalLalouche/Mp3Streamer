package backend.module

import backend.mb.MbModule
import backend.recon.ReconModule
import backend.search.SearchModule
import net.codingwell.scalaguice.ScalaModule
import songs.SongsModule

object AllModules extends ScalaModule {
  override def configure(): Unit = {
    install(MbModule)
    install(SongsModule)
    install(SearchModule)
    install(ReconModule)
  }
}
