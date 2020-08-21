package backend.lyrics.retrievers

import org.scalatest.FreeSpec

class MusixMatchParserTest extends FreeSpec with LyricsSpec {
  override private[retrievers] def parser = MusixMatchParser.parser

  "parse" in {
    verifyLyrics("musixmatch_lyrics")
  }
}
