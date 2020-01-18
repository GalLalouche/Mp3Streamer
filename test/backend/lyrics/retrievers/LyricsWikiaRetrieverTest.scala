package backend.lyrics.retrievers

import models.FakeModelFactory
import org.scalatest.FreeSpec

class LyricsWikiaRetrieverTest extends FreeSpec with LyricsSpec {
  private val fakeModelFactory = new FakeModelFactory

  "getUrl" in {
    LyricsWikiaRetriever.url.urlFor(
      fakeModelFactory.song(artistName = "Foo Bar", title = "Bazz Qux")) shouldReturn
        "http://lyrics.wikia.com/wiki/Foo_Bar:Bazz_Qux"
  }
  "lyrics" - {
    "has lyrics" in {
      verifyLyrics(
        LyricsWikiaRetriever.parser(getDocument("lyrics_wikia_lyrics.html"), fakeModelFactory.song()),
        "lyrics_wikia_lyrics.txt",
      )
    }
    "instrumental" in {
      LyricsWikiaRetriever.parser(
        getDocument("lyrics_wikia_instrumental.html"), fakeModelFactory.song()) should be an instrumental
    }
    "No license returns error" in {
      verifyError(
        LyricsWikiaRetriever.parser(getDocument("lyrics_wikia_no_license.html"), fakeModelFactory.song()))
    }
  }
}
