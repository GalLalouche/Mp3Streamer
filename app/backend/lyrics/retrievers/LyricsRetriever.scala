package backend.lyrics.retrievers

import backend.lyrics.Lyrics
import backend.Retriever
import models.Song

import scala.concurrent.Future

private[lyrics] trait LyricsRetriever extends Retriever[Song, Lyrics]{
  // For point free style
  def get: Song => Future[Lyrics]
  override def apply(v1: Song) = get(v1)
}
