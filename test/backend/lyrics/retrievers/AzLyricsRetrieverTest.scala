package backend.lyrics.retrievers

import backend.module.TestModuleConfiguration
import models.FakeModelFactory
import net.codingwell.scalaguice.InjectorExtensions._

class AzLyricsRetrieverTest extends LyricsSpec {
  private val fakeModelFactory = new FakeModelFactory
  private val $ = TestModuleConfiguration().injector.instance[AzLyricsRetriever]
  "getUrl" in {
    $.getUrl(fakeModelFactory.song(artistName = "Guns n' Roses", title = "Paradise City")) shouldReturn
        "https://www.azlyrics.com/lyrics/gunsnroses/paradisecity.html"
  }
  "fromHtml" - {
    "has lyrics" in {
      verifyLyrics($.parser(getDocument("az_lyrics.html"), fakeModelFactory.song()),
        "Ascending in sectarian rapture",
        "To pierce the eye ov JHWH")
    }
  }
}
