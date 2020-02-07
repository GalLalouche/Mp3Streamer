package backend.lyrics.retrievers

import org.scalatest.FreeSpec

class DarkLyricsRetrieverTest extends FreeSpec with LyricsSpec {
  override private[retrievers] def parser = DarkLyricsRetriever.parser
  "getUrl" in {
    DarkLyricsRetriever.url.urlFor(
      factory.song(artistName = "foo bar", albumName = "bazz qux", track = 5)) shouldReturn
        "http://www.darklyrics.com/lyrics/foobar/bazzqux.html#5"
  }
  "fromHtml" - {
    "vanilla" - {
      def testEntireLyrics(i: Int) =
        verifyLyrics("dark_lyrics", s"dark_lyrics_$i", trackNumber = i)
      "first song" in testEntireLyrics(1)
      "middle song" in testEntireLyrics(8)
      "last song" in testEntireLyrics(11)
    }
    "instrumental" in {verifyInstrumental("dark_lyrics", 4)}
    "instrumental lowercase" in {verifyInstrumental("dark_lyrics3", 1)}
    "instrumental in part of song" in {verifyLyrics("dark_lyrics2", "dark_lyrics_9", 9)}
  }
}
