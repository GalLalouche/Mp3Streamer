package backend.lyrics.retrievers.bandcamp

import org.scalatest.FreeSpec

import backend.lyrics.retrievers.LyricsSpec

class AlbumParserTest extends FreeSpec with LyricsSpec {
  private[retrievers] override def parser = AlbumParser
  "English" - {
    verifyLyrics("bandcamp_album_english", "bandcamp_english", trackNumber = 2)
  }
  "Hebrew" in {
    verifyLyrics("bandcamp_album_hebrew", "bandcamp_hebrew", trackNumber = 3)
  }
}
