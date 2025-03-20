package backend.module

import java.util.logging.LogManager

import backend.external.ExternalModule
import backend.lyrics.LyricsModule
import backend.mb.MbModule
import backend.new_albums.NewAlbumsModule
import backend.recent.RecentModule
import backend.recon.ReconModule
import backend.scorer.ScorerModule
import mains.fixer.FixerModule
import net.codingwell.scalaguice.ScalaModule
import songs.SongsModule

import common.io.google.GoogleModule

object AllModules extends ScalaModule {
  LogManager.getLogManager.readConfiguration(getClass.getResourceAsStream("/logging.properties"))

  override def configure(): Unit = {
    install(ExternalModule)
    install(FixerModule)
    install(GoogleModule)
    install(LyricsModule)
    install(MbModule)
    install(NewAlbumsModule)
    install(RecentModule)
    install(ReconModule)
    install(ScorerModule)
    install(SongsModule)
  }
}
