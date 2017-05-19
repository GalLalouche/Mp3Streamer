package backend.lyrics.retrievers

import backend.Url
import backend.lyrics.Lyrics
import models.Song

import scala.concurrent.Future

private[lyrics] trait LyricsRetriever {
  def doesUrlMatchHost(url: Url): Boolean
  def find(s: Song): Future[Lyrics]
  // parse needs a song because some sites offer more than a single lyric per page
  // TODO this should check if URL matches host before
  def parse(url: Url, s: Song): Future[Lyrics]
}
