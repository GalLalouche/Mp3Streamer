package backend.lyrics.retrievers

import backend.search.FakeModelFactory

class LyricsWikiaRetrieverTest extends LyricsSpec {
  private val fakeModelFactory = new FakeModelFactory
  private val $ = new LyricsWikiaRetriever()

  "getUrl" in {
    $.getUrl(fakeModelFactory.song(artistName = "Foo Bar", title = "Bazz Qux")) shouldReturn
        "http://lyrics.wikia.com/wiki/Foo_Bar:Bazz_Qux"
  }
  "lyrics" - {
    "has lyrics" in {
      verifyLyrics($.fromHtml(getDocument("lyrics_wikia_lyrics.html"), fakeModelFactory.song()),
        "Daddy's flown across the ocean",
        "All in all it was all just bricks in the wall")
    }
    "instrumental" in {
      $.fromHtml(getDocument("lyrics_wikia_instrumental.html"), fakeModelFactory.song()) should be an instrumental
    }
  }
}
