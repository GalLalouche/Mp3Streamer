package backend.lyrics.retrievers

import backend.logging.LoggerProvider
import backend.lyrics.Lyrics
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.{FutureInstances, ListInstances}
import scalaz.syntax.ToMonadErrorOps

private[lyrics] class CompositeLyricsRetriever(retrievers: List[LyricsRetriever])
    (implicit ec: ExecutionContext, loggerProvider: LoggerProvider) extends LyricsRetriever
    with FutureInstances with ListInstances with ToMonadErrorOps {
  // TODO better errors when no parser is found
  override def apply(s: Song): Future[Lyrics] =
    retrievers.foldLeft[Future[Lyrics]](retrievers.head.apply(s))((x, y) => x.handleError(e => {
      loggerProvider.logger.error("Failed to parse lyrics: ", e)
      y.apply(s)
    }))
  def this(retrievers: LyricsRetriever*)(implicit ec: ExecutionContext, lp: LoggerProvider) =
    this(retrievers.toList)
}
