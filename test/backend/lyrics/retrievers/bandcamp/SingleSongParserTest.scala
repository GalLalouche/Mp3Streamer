package backend.lyrics.retrievers.bandcamp

import backend.lyrics.retrievers.LyricsSpec
import models.FakeModelFactory
import org.scalatest.FreeSpec
import org.scalatest.mockito.MockitoSugar

class SingleSongParserTest extends FreeSpec with LyricsSpec with MockitoSugar {
  private val fakeModelFactory = new FakeModelFactory
  "parse" - {
    "english" in {
      verifyLyrics(
        SingleSongParser(getDocument("bandcamp_song_english.html"), fakeModelFactory.song()),
        "bandcamp_english.txt",
      )
    }
    "hebrew" in { // SBT has issues with hebrew files it seems
      verifyLyrics(
        SingleSongParser(getDocument("bandcamp_song_hebrew.html"), fakeModelFactory.song()),
        "bandcamp_hebrew.txt"
      )
    }
  }
}
