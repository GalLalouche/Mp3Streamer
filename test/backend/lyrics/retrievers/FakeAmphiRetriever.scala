package backend.lyrics.retrievers

import backend.lyrics.{Instrumental, LyricsUrl}
import io.lemonlabs.uri.Url
import models.Song

import scala.concurrent.Future

private class FakeAmphiRetriever(songsToFind: Song, urlToMatch: Url, instrumentalText: String)
    extends AmphiRetriever {
  var numberOfTimesInvoked = 0 // Mockito spy is throwing NPE for some reason
  override def get = ???
  override def apply(s: Song) = parse(urlToMatch, s)
  override def doesUrlMatchHost = url => {
    numberOfTimesInvoked += 1
    urlToMatch == url
  }
  override def parse = {
    numberOfTimesInvoked += 1
    (url: Url, s: Song) =>
      Future.successful {
        if (s == songsToFind && url == urlToMatch)
          RetrievedLyricsResult.RetrievedLyrics(
            Instrumental(instrumentalText, LyricsUrl.Url(url)),
          )
        else
          RetrievedLyricsResult.NoLyrics
      }
  }
}
