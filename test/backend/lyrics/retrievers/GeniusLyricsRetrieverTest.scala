package backend.lyrics.retrievers

import models.FakeModelFactory
import net.codingwell.scalaguice.InjectorExtensions._

class GeniusLyricsRetrieverTest extends LyricsSpec {
  private val fakeModelFactory = new FakeModelFactory
  private val $ = config.injector.instance[GeniusLyricsRetriever]
  "getUrl" in {
    $.getUrl(fakeModelFactory.song(artistName = "Guns n' Roses", title = "Paradise City")) shouldReturn
        "https://genius.com/guns-n-roses-paradise-city-lyrics"
  }
  "fromHtml" - {
    "has lyrics" in {
      verifyLyrics($.fromHtml(getDocument("genius_lyrics.html"), fakeModelFactory.song()),
        "Take me down to the Paradise City",
        "Baby")
    }
  }
}
