package backend.lyrics.retrievers

import backend.Url
import backend.lyrics.Lyrics
import models.Song

import scala.concurrent.Future

private class SingleHostUrlHelper(
    singleHostUrl: SingleHostUrl,
    f: (Url, Song) => Future[Lyrics]
) {
  def doesUrlMatchHost: Url => Boolean = _.address.startsWith(singleHostUrl.hostPrefix)
  def get: Song => Future[Lyrics] = s => f(Url(singleHostUrl.urlFor(s)), s)
}
