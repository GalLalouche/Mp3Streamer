package backend.lyrics.retrievers

import search.FakeModelFactory

class AzLyricsRetrieverTest extends LyricsSpec {
  private val fakeModelFactory = new FakeModelFactory
  private val $ = new AzLyricsRetriever()
  "getUrl" in {
    $.getUrl(fakeModelFactory.song(artistName = "Guns n' Roses", title = "Paradise City")) shouldReturn
        "https://www.azlyrics.com/lyrics/gunsnroses/paradisecity.html"
  }
  "fromHtml" - {
    "has lyrics" in {
      verifyLyrics($.fromHtml(getDocument("az_lyrics.html"), fakeModelFactory.song()),
        "Ascending in sectarian rapture",
        "To pierce the eye ov JHWH")
    }
  }
}
