package backend.lyrics.retrievers

import search.FakeModelFactory

class AzLyricsRetrieverTest extends LyricsSpec {
  private val $ = new AzLyricsRetriever()
  "getUrl" in {
    $.getUrl(FakeModelFactory.mockSong(artistName = "Guns n' Roses", title = "Paradise City")) shouldReturn
        "http://www.azlyrics.com/lyrics/gunsnroses/paradisecity.html"
  }
  "fromHtml" - {
    "has lyrics" in {
      verifyLyrics($.fromHtml(getDocument("az_lyrics.html"), FakeModelFactory.mockSong()),
        "Ascending in sectarian rapture",
        "To pierce the eye ov JHWH")
    }
  }
}
