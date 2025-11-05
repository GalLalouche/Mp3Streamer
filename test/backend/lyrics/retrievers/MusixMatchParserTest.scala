package backend.lyrics.retrievers

import org.scalatest.freespec.AnyFreeSpec

class MusixMatchParserTest extends AnyFreeSpec with LyricsSpec {
  private[retrievers] override def parser = MusixMatchParser.parser

  "parse" in {
    verifyLyrics("musixmatch_lyrics")
  }
}
