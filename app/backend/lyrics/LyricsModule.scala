package backend.lyrics

import backend.lyrics.retrievers.genius.GeniusModule
import net.codingwell.scalaguice.ScalaModule

object LyricsModule extends ScalaModule {
  override def configure(): Unit = {
    install(GeniusModule)
  }
}
