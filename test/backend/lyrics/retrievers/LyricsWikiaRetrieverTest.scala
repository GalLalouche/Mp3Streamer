package backend.lyrics.retrievers

import backend.module.TestModuleConfiguration
import models.FakeModelFactory
import net.codingwell.scalaguice.InjectorExtensions._

class LyricsWikiaRetrieverTest extends LyricsSpec {
  private val fakeModelFactory = new FakeModelFactory
  private val $ = TestModuleConfiguration().injector.instance[LyricsWikiaRetriever]

  "getUrl" in {
    $.url.urlFor(fakeModelFactory.song(artistName = "Foo Bar", title = "Bazz Qux")) shouldReturn
        "http://lyrics.wikia.com/wiki/Foo_Bar:Bazz_Qux"
  }
  "lyrics" - {
    "has lyrics" in {
      verifyLyrics($.parser(getDocument("lyrics_wikia_lyrics.html"), fakeModelFactory.song()),
        "Daddy's flown across the ocean",
        "All in all it was all just bricks in the wall")
    }
    "instrumental" in {
      $.parser(getDocument("lyrics_wikia_instrumental.html"), fakeModelFactory.song()) should be an instrumental
    }
    "No license returns error" in {
      verifyError($.parser(getDocument("lyrics_wikia_no_license.html"), fakeModelFactory.song()))
    }
  }
}
