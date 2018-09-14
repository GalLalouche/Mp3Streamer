package backend.lyrics.retrievers

import backend.logging.Logger
import backend.lyrics.Lyrics
import common.rich.func.ToMoreMonadErrorOps
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.{FutureInstances, ListInstances}

private[lyrics] class CompositeLyricsRetriever @Inject()(
    ec: ExecutionContext,
    logger: Logger,
    retrievers: Seq[LyricsRetriever],
) extends LyricsRetriever
    with FutureInstances with ListInstances with ToMoreMonadErrorOps {
  private implicit val iec: ExecutionContext = ec

  override val get = s =>
    retrievers.foldLeft[Future[Lyrics]](retrievers.head(s))((result, nextRetriever) => result.listenError({
      case _: NoStoredLyricException => () // TODO don't use exceptions :\
      case e: Exception => logger.error("Failed to parse lyrics: ", e)
    }) orElseTry nextRetriever(s))
}
