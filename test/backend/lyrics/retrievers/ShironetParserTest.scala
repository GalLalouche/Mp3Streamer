package backend.lyrics.retrievers

import models.FakeModelFactory
import org.scalatest.FreeSpec

class ShironetParserTest extends FreeSpec with LyricsSpec {
  private val fake = new FakeModelFactory
  "Lyrics" in {
    verifyLyrics(ShironetParser.parser(getDocument("shironet.html"), fake.song()), "shironet.txt")
  }
}
