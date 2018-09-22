package backend.lyrics.retrievers

import backend.Retriever
import models.Song

import scala.concurrent.Future

private[lyrics] trait LyricsRetriever extends Retriever[Song, RetrievedLyricsResult] {
  // For point free style
  def get: Song => Future[RetrievedLyricsResult]
  override def apply(v1: Song) = get(v1)
}
