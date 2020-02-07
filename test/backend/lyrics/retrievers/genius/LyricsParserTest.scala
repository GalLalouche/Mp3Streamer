package backend.lyrics.retrievers.genius

import backend.lyrics.retrievers.{LyricsSpec, SingleHostParser}
import org.scalatest.FreeSpec

class LyricsParserTest extends FreeSpec with LyricsSpec {
  override private[retrievers] def parser: SingleHostParser = LyricsParser
  "fromHtml" - {
    "has lyrics" in {verifyLyrics("lyrics1")}
    "has lyrics2" in {verifyLyrics("lyrics2")}
    "Ignores parenthesis comments" in {verifyLyrics("parens")}
    "instrumental" - {
      "[instrumental]" in {verifyInstrumental("instrumental1")}
      "no brackets" in {verifyInstrumental("instrumental2")}
    }
  }
}
