package backend.lyrics.retrievers

import backend.Url
import models.Song

import scala.concurrent.Future

private[lyrics] trait HtmlRetriever extends LyricsRetriever {
  // For point free style
  def doesUrlMatchHost: Url => Boolean
  def parse: (Url, Song) => Future[RetrievedLyricsResult]
}
