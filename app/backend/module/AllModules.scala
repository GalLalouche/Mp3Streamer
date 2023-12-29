package backend.module

import java.util.logging.LogManager

import backend.albums.NewAlbumModule
import backend.external.ExternalModule
import backend.lyrics.LyricsModule
import backend.mb.MbModule
import backend.recent.RecentModule
import backend.recon.ReconModule
import backend.scorer.ScorerModule
import controllers.ControllerModule
import net.codingwell.scalaguice.ScalaModule
import songs.SongsModule

import common.io.google.GoogleModule

object AllModules extends ScalaModule {
  LogManager.getLogManager.readConfiguration(getClass.getResourceAsStream("/logging.properties"))

  override def configure(): Unit = {
    install(GoogleModule)
    install(ExternalModule)
    install(LyricsModule)
    install(MbModule)
    install(NewAlbumModule)
    install(RecentModule)
    install(ReconModule)
    install(ScorerModule)
    install(SongsModule)

    install(ControllerModule)
  }
}
