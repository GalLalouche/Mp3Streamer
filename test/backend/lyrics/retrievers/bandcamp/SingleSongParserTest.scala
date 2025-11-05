package backend.lyrics.retrievers.bandcamp

import backend.lyrics.retrievers.{LyricsSpec, SingleHostParser}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar

class SingleSongParserTest extends AnyFreeSpec with LyricsSpec with MockitoSugar {
  private[retrievers] override def parser: SingleHostParser = SingleSongParser
  "English" in {
    verifyLyrics("bandcamp_song_english", "bandcamp_english")
  }
  "Hebrew" in {
    verifyLyrics("bandcamp_song_hebrew", "bandcamp_hebrew")
  }
}
