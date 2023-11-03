package backend.lyrics.retrievers

import scala.concurrent.Future

import backend.Url
import models.Song

private class SingleHostUrlHelper(
    singleHostUrl: SingleHostUrl,
    f: (Url, Song) => Future[RetrievedLyricsResult],
) {
  def doesUrlMatchHost: Url => Boolean = _.address.startsWith(singleHostUrl.hostPrefix)
  def get: Song => Future[RetrievedLyricsResult] = s => f(Url(singleHostUrl.urlFor(s)), s)
}
