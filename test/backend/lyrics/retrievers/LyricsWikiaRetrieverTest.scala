package backend.lyrics.retrievers

import models.FakeModelFactory
import org.scalatest.FreeSpec

class LyricsWikiaRetrieverTest extends FreeSpec with LyricsSpec {
  private val fakeModelFactory = new FakeModelFactory
  override private[retrievers] def parser = LyricsWikiaRetriever.parser

  "getUrl" in {
    LyricsWikiaRetriever.url.urlFor(
      fakeModelFactory.song(artistName = "Foo Bar", title = "Bazz Qux")) shouldReturn
        "http://lyrics.wikia.com/wiki/Foo_Bar:Bazz_Qux"
  }
  "lyrics" - {
    "has lyrics" in {verifyLyrics("lyrics_wikia_lyrics")}
    "instrumental" in {verifyInstrumental("lyrics_wikia_instrumental")}
    "No license returns error" in {verifyError("lyrics_wikia_no_license")}
  }
}
