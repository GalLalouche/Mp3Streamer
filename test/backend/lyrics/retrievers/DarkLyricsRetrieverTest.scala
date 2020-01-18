package backend.lyrics.retrievers

import models.FakeModelFactory
import org.scalatest.FreeSpec

class DarkLyricsRetrieverTest extends FreeSpec with LyricsSpec {
  private val fakeModelFactory = new FakeModelFactory
  "getUrl" in {
    DarkLyricsRetriever.url.urlFor(
      fakeModelFactory.song(artistName = "foo bar", albumName = "bazz qux", track = 5)) shouldReturn
        "http://www.darklyrics.com/lyrics/foobar/bazzqux.html#5"
  }
  "fromHtml" - {
    def getHtml(trackNumber: Int, html: String = "dark_lyrics.html") =
      DarkLyricsRetriever.parser(getDocument(html), fakeModelFactory.song(track = trackNumber))
    "vanilla" - {
      def testEntireLyrics(i: Int) = verifyLyrics(getHtml(i), s"dark_lyrics_$i.txt")
      "first song" in testEntireLyrics(1)
      "middle song" in testEntireLyrics(8)
      "last song" in testEntireLyrics(11)
    }
    "instrumental" in {
      getHtml(4) should be an instrumental
    }
    "instrumental lowercase" in {
      getHtml(1, "dark_lyrics3.html") should be an instrumental
    }
    "instrumental in part of song" in {
      verifyLyrics(getHtml(9, "dark_lyrics2.html"), "dark_lyrics_9.txt")
    }
  }
}
