package backend.lyrics.retrievers

import backend.configs.TestModuleConfiguration
import models.FakeModelFactory
import net.codingwell.scalaguice.InjectorExtensions._

class DarkLyricsRetrieverTest extends LyricsSpec {
  private val fakeModelFactory = new FakeModelFactory
  private val $ = TestModuleConfiguration().injector.instance[DarkLyricsRetriever]
  "getUrl" in {
    $.getUrl(fakeModelFactory.song(artistName = "foo bar", albumName = "bazz qux", track = 5)) shouldReturn
        "http://www.darklyrics.com/lyrics/foobar/bazzqux.html#5"
  }
  "fromHtml" - {
    def getHtml(trackNumber: Int, html: String = "dark_lyrics.html") =
      $.fromHtml(getDocument(html), fakeModelFactory.song(track = trackNumber))
    "first song" in {
      verifyLyrics(getHtml(1),
        "<i>[Samples from the film \"The Dead\", an adaptation of James Joyce's short story from his book]</i>",
        "\"I know all about the honor of God, Mary Jane.\"")
    }
    "middle song" in {
      verifyLyrics(getHtml(8), "Daybreak", "and you can tell your stepfather I said so")
    }
    "last song" in {
      verifyLyrics(getHtml(11), "Falling through pages of Martens on angels", "And I'll never be open again")
    }
    "instrumental" in {
      getHtml(4) should be an instrumental
    }
    "instrumental in part of song" in {
      verifyLyrics(getHtml(9, "dark_lyrics2.html"), "<i>[I. The Message]</i>", "How will it be?")
    }
  }
}
