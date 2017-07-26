package backend.lyrics.retrievers
import backend.Url
import backend.lyrics.Instrumental
import models.Song

import scala.concurrent.Future

private[lyrics] trait DefaultInstrumental extends LyricsRetriever {
  protected def isInstrumental(s: Song): Boolean
  protected val defaultType: String
  override def find(s: Song) = {
    if (isInstrumental(s))
      Future.successful(Instrumental(defaultType))
    else
      Future.failed(new IllegalArgumentException(s"Not a $defaultType song"))
  }
  override def doesUrlMatchHost(url: Url) = false
  override def parse(url: Url, s: Song) =
    Future.failed(new UnsupportedOperationException("NoUrls"))
}
