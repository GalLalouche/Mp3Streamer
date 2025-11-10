package backend.lyrics.retrievers

import org.scalatest.freespec.AnyFreeSpec

class AzLyricsRetrieverTest extends AnyFreeSpec with LyricsSpec {
  private[retrievers] override def parser = AzLyricsRetriever.parser

  "getUrl" in {
    AzLyricsRetriever.url.urlFor(
      factory.song(artistName = "Guns n' Roses", title = "Paradise City"),
    ) shouldReturn
      "https://www.azlyrics.com/lyrics/gunsnroses/paradisecity.html"
  }
  "fromHtml" - {
    "has lyrics" in verifyLyrics("az_lyrics")
    "has lyrics and image" in verifyLyrics("az_lyrics_with_image")
  }
  "has no lyrics" in verifyNoLyrics("az_no_lyrics")
}
