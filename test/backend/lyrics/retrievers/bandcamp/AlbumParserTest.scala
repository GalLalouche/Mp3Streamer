package backend.lyrics.retrievers.bandcamp

import backend.lyrics.retrievers.LyricsSpec
import org.scalatest.FreeSpec

class AlbumParserTest extends FreeSpec with LyricsSpec {
  private[retrievers] override def parser = AlbumParser
  "English" - {
    verifyLyrics("bandcamp_album_english", "bandcamp_english", trackNumber = 2)
  }
  "Hebrew" in {
    verifyLyrics("bandcamp_album_hebrew", "bandcamp_hebrew", trackNumber = 3)
  }
  "Instrumental" in {
    verifyInstrumental("bandcamp_instrumental", trackNumber = 5)
  }
}
