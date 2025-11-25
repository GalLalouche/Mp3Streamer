package backend.lyrics.retrievers.genius

import backend.lyrics.retrievers.{LyricsSpec, SingleHostParser}
import org.scalatest.freespec.AnyFreeSpec

class LyricsParserTest extends AnyFreeSpec with LyricsSpec {
  private[retrievers] override def parser: SingleHostParser = LyricsParser
  "fromHtml" - {
    "lyrics" in verifyLyrics("lyrics1")
    "lyrics2" in verifyLyrics("lyrics2")
    "2022" in verifyLyrics("2022")
    "ads" in verifyLyrics("ads")
    "spacing" in verifyLyrics("spacing")
    "italics" in verifyLyrics("italics")
    "Ignores parenthesis comments" in verifyLyrics("parens")
    "instrumental" in verifyInstrumental("instrumental")
    "More BS brackets" in verifyLyrics("bs_brackets")
    "untranscribed" in verifyNoLyrics("untranscribed")
  }
}
