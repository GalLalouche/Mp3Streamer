package backend.lyrics.retrievers

import io.lemonlabs.uri.Url
import models.Song

import scala.concurrent.Future

private class SingleHostUrlHelper(
    singleHostUrl: SingleHostUrl,
    f: (Url, Song) => Future[RetrievedLyricsResult],
) {
  def doesUrlMatchHost: Url => Boolean = _.toStringPunycode.startsWith(singleHostUrl.hostPrefix)
  def get: Song => Future[RetrievedLyricsResult] = s => f(Url(singleHostUrl.urlFor(s)), s)
}
