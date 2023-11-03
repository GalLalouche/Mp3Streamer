package backend.lyrics.retrievers.bandcamp

import org.scalatest.mockito.MockitoSugar
import org.scalatest.FreeSpec

import backend.lyrics.retrievers.{LyricsSpec, SingleHostParser}

class SingleSongParserTest extends FreeSpec with LyricsSpec with MockitoSugar {
  private[retrievers] override def parser: SingleHostParser = SingleSongParser
  "English" in {
    verifyLyrics("bandcamp_song_english", "bandcamp_english")
  }
  "Hebrew" in {
    verifyLyrics("bandcamp_song_hebrew", "bandcamp_hebrew")
  }
}
