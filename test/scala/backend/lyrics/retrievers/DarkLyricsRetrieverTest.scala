package backend.lyrics.retrievers

import search.Models

class DarkLyricsRetrieverTest extends LyricsSpec {
  private val $ = new DarkLyricsRetriever
  "getUrl" in {
    $.getUrl(Models.mockSong(artistName = "foo bar", albumName = "bazz qux", track = 5)) shouldReturn
        "http://www.darklyrics.com/lyrics/foobar/bazzqux.html#5"
  }
  "fromHtml" - {
    def getHtml(trackNumber: Int) = $.fromHtml(getDocument("dark_lyrics.html"), Models.mockSong(track = trackNumber))
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
  }
}
