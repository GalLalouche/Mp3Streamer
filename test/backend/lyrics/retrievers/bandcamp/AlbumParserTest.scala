package backend.lyrics.retrievers.bandcamp

import backend.lyrics.retrievers.LyricsSpec
import models.FakeModelFactory
import org.scalatest.FreeSpec

class AlbumParserTest extends FreeSpec with LyricsSpec {
  private val fakeModelFactory = new FakeModelFactory
  "English" in {
    verifyLyrics(
      AlbumParser(getDocument("bandcamp_album_english.html"), fakeModelFactory.song(track = 2)),
      "bandcamp_english.txt",
    )
  }
  "Hebrew" in {
    verifyLyrics(
      AlbumParser(getDocument("bandcamp_album_hebrew.html"), fakeModelFactory.song(track = 3)),
      "bandcamp_hebrew.txt",
    )
  }
}
