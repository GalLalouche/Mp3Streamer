package backend.lyrics.retrievers

import search.FakeModelFactory

class LyricsWikiaRetrieverTest extends LyricsSpec {
  private val $ = new LyricsWikiaRetriever()

  "getUrl" in {
    $.getUrl(FakeModelFactory.mockSong(artistName = "Foo Bar", title = "Bazz Qux")) shouldReturn
        "http://lyrics.wikia.com/wiki/Foo_Bar:Bazz_Qux"
  }
  "lyrics" - {
    "has lyrics" in {
      verifyLyrics($.fromHtml(getDocument("lyrics_wikia_lyrics.html"), FakeModelFactory.mockSong()),
        "Daddy's flown across the ocean",
        "All in all it was all just bricks in the wall")
    }
    "instrumental" in {
      $.fromHtml(getDocument("lyrics_wikia_instrumental.html"), FakeModelFactory.mockSong()) should be an instrumental
    }
  }
}
