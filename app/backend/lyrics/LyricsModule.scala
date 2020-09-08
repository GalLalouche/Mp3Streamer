package backend.lyrics

import backend.lyrics.retrievers.genius.GeniusModule
import backend.lyrics.retrievers.RetrieversModule
import net.codingwell.scalaguice.ScalaModule

object LyricsModule extends ScalaModule {
  override def configure(): Unit = {
    install(RetrieversModule)
    install(GeniusModule)

    bind[LyricsStorage].to[SlickLyricsStorage]
  }
}
