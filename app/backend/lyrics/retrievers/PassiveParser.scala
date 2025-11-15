package backend.lyrics.retrievers

import io.lemonlabs.uri.Url
import models.Song

import scala.concurrent.Future

/** Can parse lyrics given a URL but can't actively search for a URL based on song. */
private[lyrics] trait PassiveParser {
  // For point free style
  def doesUrlMatchHost: Url => Boolean
  def parse: (Url, Song) => Future[RetrievedLyricsResult]
}
