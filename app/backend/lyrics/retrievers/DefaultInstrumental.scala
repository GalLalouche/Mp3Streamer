package backend.lyrics.retrievers

import backend.lyrics.Instrumental
import models.Song

import scala.concurrent.Future

private[lyrics] trait DefaultInstrumental extends LyricsRetriever {
  protected def isInstrumental(s: Song): Boolean
  protected def defaultType: String
  protected def instrumental: Instrumental = Instrumental(s"Default for $defaultType")
  override def get: Song => Future[RetrievedLyricsResult] = s => Future successful {
    if (isInstrumental(s))
      RetrievedLyricsResult.RetrievedLyrics(instrumental)
    else
      RetrievedLyricsResult.NoLyrics
  }
}
