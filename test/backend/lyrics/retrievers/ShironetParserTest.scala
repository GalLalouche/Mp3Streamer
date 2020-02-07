package backend.lyrics.retrievers

import org.scalatest.FreeSpec

class ShironetParserTest extends FreeSpec with LyricsSpec {
  override private[retrievers] def parser = ShironetParser.parser
  "Lyrics" in {verifyLyrics("shironet")}
}
