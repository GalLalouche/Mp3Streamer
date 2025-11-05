package backend.lyrics.retrievers

import org.scalatest.freespec.AnyFreeSpec

class ShironetParserTest extends AnyFreeSpec with LyricsSpec {
  private[retrievers] override def parser = ShironetParser.parser
  "Lyrics" in verifyLyrics("shironet")
  "Lyrics2" in verifyLyrics("shironet2")
}
