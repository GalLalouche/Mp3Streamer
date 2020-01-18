package backend.lyrics.retrievers.genius

import backend.lyrics.retrievers.LyricsSpec
import models.FakeModelFactory
import org.scalatest.FreeSpec

class LyricsParserTest extends FreeSpec with LyricsSpec {
  private val fakeModelFactory = new FakeModelFactory
  "fromHtml" - {
    "has lyrics" in {
      verifyLyrics(
        LyricsParser(getDocument("lyrics1.html"), fakeModelFactory.song()),
        "lyrics1.txt",
      )
    }
    "has lyrics2" in {
      verifyLyrics(
        LyricsParser(getDocument("lyrics2.html"), fakeModelFactory.song()),
        "lyrics2.txt",
      )
    }
    "instrumental" - {
      "[instrumental]" in {
        LyricsParser(getDocument("instrumental1.html"), fakeModelFactory.song()) should be an instrumental
      }
      "no brackets" in {
        LyricsParser(getDocument("instrumental2.html"), fakeModelFactory.song()) should be an instrumental
      }
    }
  }
}
