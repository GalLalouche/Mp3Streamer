package backend.lyrics.retrievers.genius

import backend.lyrics.retrievers.LyricsRetrieverIntegrationTemplate

class IntegrationTest extends LyricsRetrieverIntegrationTemplate {
  "Unignore to run" ignore {
    go[GeniusLyricsRetriever](
      file =
        """G:\Media\Music\Rock\Hard-Rock\Guns n' Roses\1987 Appetite for Destruction\06 - Paradise City.mp3""",
      source = "GeniusLyrics",
      expectedLyricsPath = "lyrics1.txt",
      expectedUrl = "https://genius.com/Guns-n-roses-paradise-city-lyrics",
      extraModule = GeniusModule,
    )
  }
}
