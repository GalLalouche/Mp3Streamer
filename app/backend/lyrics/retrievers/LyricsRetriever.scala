package backend.lyrics.retrievers

import backend.lyrics.Lyrics
import models.Song

import scala.concurrent.Future

private[lyrics] trait LyricsRetriever {
  def find(s: Song): Future[Lyrics]
}
