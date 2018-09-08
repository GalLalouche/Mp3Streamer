package backend.lyrics.retrievers

import models.FakeModelFactory

class LyricsWikiaRetrieverTest extends LyricsSpec {
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
        "Daddy's flown across the ocean",
        "All in all it was all just bricks in the wall")
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
