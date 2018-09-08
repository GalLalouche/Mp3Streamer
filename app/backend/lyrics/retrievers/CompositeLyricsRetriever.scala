package backend.lyrics.retrievers

import backend.logging.Logger
import backend.lyrics.Lyrics
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.{FutureInstances, ListInstances}
import scalaz.syntax.ToMonadErrorOps

private[lyrics] class CompositeLyricsRetriever @Inject()(
    ec: ExecutionContext,
    logger: Logger,
    retrievers: Seq[LyricsRetriever],
) extends LyricsRetriever
    with FutureInstances with ListInstances with ToMonadErrorOps {
  private implicit val iec: ExecutionContext = ec

  // TODO better errors when no parser is found
  override val get = s =>
    retrievers.foldLeft[Future[Lyrics]](retrievers.head(s))((x, y) => x.handleError(e => {
      logger.error("Failed to parse lyrics: ", e)
      y.apply(s)
    }))
}
