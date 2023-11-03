package backend.lyrics.retrievers.bandcamp

import backend.lyrics.retrievers.LyricsRetrieverIntegrationTemplate

class IntegrationTest extends LyricsRetrieverIntegrationTemplate {
  "Unignore to run" ignore {
    go[BandcampAlbumRetriever](
      file =
        """G:\Media\Music\Rock\Dark Cabaret\The Dresden Dolls\2003 The Dresden Dolls\02 - Girl Anachronism.mp3""",
      source = "Bandcamp",
      expectedLyricsPath = "bandcamp_english.txt",
      expectedUrl = "https://dresdendolls.bandcamp.com/album/the-dresden-dolls",
    )
  }
}
