package backend.module

import backend.external.ExternalModule
import backend.mb.MbModule
import backend.recent.RecentModule
import backend.recon.ReconModule
import backend.search.SearchModule
import controllers.ControllerModule
import net.codingwell.scalaguice.ScalaModule
import songs.SongsModule

object AllModules extends ScalaModule {
  override def configure(): Unit = {
    install(ExternalModule)
    install(MbModule)
    install(RecentModule)
    install(ReconModule)
    install(SearchModule)
    install(SongsModule)
    install(ControllerModule)
  }
}
