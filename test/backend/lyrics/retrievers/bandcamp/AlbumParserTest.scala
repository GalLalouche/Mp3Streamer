package backend.lyrics.retrievers.bandcamp

import backend.lyrics.retrievers.LyricsSpec
import org.scalatest.FreeSpec

class AlbumParserTest extends FreeSpec with LyricsSpec {
  override private[retrievers] def parser = AlbumParser
  "English" in {
    verifyLyrics("bandcamp_album_english", "bandcamp_english", trackNumber = 2)
  }
  "Hebrew" in {
    verifyLyrics("bandcamp_album_hebrew", "bandcamp_hebrew", trackNumber = 3)
  }
}
