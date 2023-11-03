package backend.lyrics.retrievers.genius

import org.scalatest.FreeSpec

import backend.lyrics.retrievers.{LyricsSpec, SingleHostParser}

class LyricsParserTest extends FreeSpec with LyricsSpec {
  private[retrievers] override def parser: SingleHostParser = LyricsParser
  "fromHtml" - {
    "lyrics" in verifyLyrics("lyrics1")
    "lyrics2" in verifyLyrics("lyrics2")
    "2022" in verifyLyrics("2022")
    "spacing" in verifyLyrics("spacing")
    "italics" in verifyLyrics("italics")
    "Ignores parenthesis comments" in verifyLyrics("parens")
    "instrumental" in verifyInstrumental("instrumental")
  }
}
