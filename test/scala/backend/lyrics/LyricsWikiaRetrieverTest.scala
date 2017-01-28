package backend.lyrics

import search.Models

class LyricsWikiaRetrieverTest extends LyricsSpec {
  private val $ = new LyricsWikiaRetriever()

  "getUrl" in {
    $.getUrl(Models.mockSong(artistName = "Foo Bar", title = "Bazz Qux")) shouldReturn
        "http://lyrics.wikia.com/wiki/Foo_Bar:Bazz_Qux"
  }
  "lyrics" - {
    "has lyrics" in {
      verifyLyrics($.fromHtml(getDocument("lyrics_wikia_lyrics.html"), Models.mockSong()),
        "Daddy's flown across the ocean",
        "All in all it was all just bricks in the wall")
    }
    "instrumental" in {
      $.fromHtml(getDocument("lyrics_wikia_instrumental.html"), Models.mockSong()) should be an instrumental
    }
  }
}
