package backend.lyrics.retrievers

import backend.Url
import models.Song

import scala.concurrent.Future

/** Can parse lyrics but can't actively search for a URL based on song. */
private[lyrics] trait PassiveParser {
  // For point free style
  def doesUrlMatchHost: Url => Boolean
  def parse: (Url, Song) => Future[RetrievedLyricsResult]
}
