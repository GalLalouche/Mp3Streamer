package backend.lyrics.retrievers.genius

import backend.lyrics.retrievers.LyricsSpec
import models.FakeModelFactory
import org.scalatest.FreeSpec

class LyricsParserTest extends FreeSpec with LyricsSpec {
  private val fakeModelFactory = new FakeModelFactory
  "fromHtml" - {
    def getLyrics(name: String) = LyricsParser(getDocument(name + ".html"), fakeModelFactory.song())
    def checkLyrics(name: String) = verifyLyrics(getLyrics(name), name + ".txt")

    "has lyrics" in {checkLyrics("lyrics1")}
    "has lyrics2" in {checkLyrics("lyrics2")}
    "Ignores parenthesis comments" in {checkLyrics("parens")}
    "instrumental" - {
      "[instrumental]" in {
        getLyrics("instrumental1") should be an instrumental
      }
      "no brackets" in {
        getLyrics("instrumental2") should be an instrumental
      }
    }
  }
}
