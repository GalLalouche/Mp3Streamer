package backend.lyrics.retrievers

import org.scalatest.FreeSpec

class MusixMatchParserTest extends FreeSpec with LyricsSpec {
  private[retrievers] override def parser = MusixMatchParser.parser

  "parse" in {
    verifyLyrics("musixmatch_lyrics")
  }
}
