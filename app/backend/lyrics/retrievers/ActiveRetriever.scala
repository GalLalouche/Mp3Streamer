package backend.lyrics.retrievers

import backend.Retriever
import models.Song

import scala.concurrent.Future

/** Can look up lyrics by songs. */
private[lyrics] trait ActiveRetriever extends Retriever[Song, RetrievedLyricsResult] {
  // For point free e
  def get: Song => Future[RetrievedLyricsResult]
  override def apply(v1: Song) = get(v1)
}
