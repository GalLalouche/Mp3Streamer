package backend.lyrics.retrievers

import models.FakeModelFactory
import org.scalatest.FreeSpec

class AzLyricsRetrieverTest extends FreeSpec with LyricsSpec {
  private val fakeModelFactory = new FakeModelFactory

  "getUrl" in {
    AzLyricsRetriever.url.urlFor(
      fakeModelFactory.song(artistName = "Guns n' Roses", title = "Paradise City")) shouldReturn
        "https://www.azlyrics.com/lyrics/gunsnroses/paradisecity.html"
  }
  "fromHtml" - {
    "has lyrics" in {
      verifyLyrics(
        AzLyricsRetriever.parser(getDocument("az_lyrics.html"), fakeModelFactory.song()),
        "az_lyrics.txt",
      )
    }
  }
}
