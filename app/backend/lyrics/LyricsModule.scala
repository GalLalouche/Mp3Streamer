package backend.lyrics

import backend.lyrics.retrievers.RetrieversModule
import backend.lyrics.retrievers.genius.GeniusModule
import net.codingwell.scalaguice.ScalaModule

object LyricsModule extends ScalaModule {
  override def configure(): Unit = {
    install(RetrieversModule)
    install(GeniusModule)

    bind[LyricsStorage].to[SlickLyricsStorage]
  }
}
