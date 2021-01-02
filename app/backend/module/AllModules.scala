package backend.module

import backend.albums.NewAlbumModule
import backend.external.ExternalModule
import backend.lyrics.LyricsModule
import backend.mb.MbModule
import backend.recent.RecentModule
import backend.recon.ReconModule
import controllers.ControllerModule
import net.codingwell.scalaguice.ScalaModule
import songs.SongsModule

object AllModules extends ScalaModule {
  override def configure(): Unit = {
    install(NewAlbumModule)
    install(ExternalModule)
    install(MbModule)
    install(RecentModule)
    install(ReconModule)
    install(SongsModule)
    install(ControllerModule)
    install(LyricsModule)
  }
}
