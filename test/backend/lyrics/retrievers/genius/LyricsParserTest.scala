package backend.lyrics.retrievers.genius

import backend.lyrics.retrievers.LyricsSpec
import models.FakeModelFactory

class LyricsParserTest extends LyricsSpec {
  private val fakeModelFactory = new FakeModelFactory
  "fromHtml" - {
    "has lyrics" in {
      verifyLyrics(LyricsParser(getDocument("lyrics1.html"), fakeModelFactory.song()),
        "Take me down to the Paradise City",
        "Baby")
    }
    "has lyrics2" in {
      verifyLyrics(LyricsParser(getDocument("lyrics2.html"), fakeModelFactory.song()),
        "The boys who kiss and bite",
        "I will be sure we shake the mountains while we dance")
    }
    "instrumental" in {
      LyricsParser(getDocument("instrumental.html"), fakeModelFactory.song()) should be an instrumental
    }
  }
}
