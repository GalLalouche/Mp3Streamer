package backend.lyrics.retrievers

import backend.lyrics.Instrumental
import models.Song

import scala.concurrent.Future

private[lyrics] trait DefaultInstrumental extends LyricsRetriever {
  protected def isInstrumental(s: Song): Boolean
  protected def defaultType: String
  override def get: Song => Future[Instrumental] = s =>
    if (isInstrumental(s))
      Future.successful(Instrumental(s"Default for $defaultType"))
    else
      Future.failed(new NoStoredLyricException(s"Not an instrumental $defaultType song"))
}
