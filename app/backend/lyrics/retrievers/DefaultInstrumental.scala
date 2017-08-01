package backend.lyrics.retrievers
import backend.lyrics.Instrumental
import models.Song

import scala.concurrent.Future

private[lyrics] trait DefaultInstrumental extends LyricsRetriever {
  protected def isInstrumental(s: Song): Boolean
  protected val defaultType: String
  override def apply(s: Song): Future[Instrumental] = {
    if (isInstrumental(s))
      Future.successful(Instrumental(s"Default for $defaultType"))
    else
      Future.failed(new IllegalArgumentException(s"Not a $defaultType song"))
  }
}
