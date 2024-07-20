package backend.lyrics.retrievers

import org.scalatest.FreeSpec

class ShironetParserTest extends FreeSpec with LyricsSpec {
  private[retrievers] override def parser = ShironetParser.parser
  "Lyrics" in verifyLyrics("shironet")
  "Lyrics2" in verifyLyrics("shironet2")
}
