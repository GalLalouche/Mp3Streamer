package backend.lyrics.retrievers

import backend.Url
import backend.lyrics.Lyrics
import models.Song

import scala.concurrent.Future

private[lyrics] trait HtmlRetriever extends LyricsRetriever {
  def doesUrlMatchHost(url: Url): Boolean
  def parse(url: Url, s: Song): Future[Lyrics]
}
