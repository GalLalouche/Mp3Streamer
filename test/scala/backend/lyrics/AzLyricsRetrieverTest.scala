package backend.lyrics

import search.Models

class AzLyricsRetrieverTest extends LyricsSpec {
  private val $ = new AzLyricsRetriever()
  "getUrl" in {
    $.getUrl(Models.mockSong(artistName = "Guns n' Roses", title = "Paradise City")) shouldReturn
        "http://www.azlyrics.com/lyrics/gunsnroses/paradisecity.html"
  }
  "fromHtml" - {
    "has lyrics" in {
      verifyLyrics($.fromHtml(getDocument("az_lyrics.html"), Models.mockSong()),
        "Ascending in sectarian rapture",
        "To pierce the eye ov JHWH")
    }
  }
}
