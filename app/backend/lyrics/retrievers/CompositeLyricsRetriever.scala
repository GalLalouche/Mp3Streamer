package backend.lyrics.retrievers

import backend.configs.Configuration
import backend.logging.Logger
import backend.lyrics.Lyrics
import models.Song
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.{FutureInstances, ListInstances}
import scalaz.syntax.ToMonadErrorOps

private[lyrics] class CompositeLyricsRetriever(retrievers: List[LyricsRetriever])
    (implicit c: Configuration) extends LyricsRetriever
    with FutureInstances with ListInstances with ToMonadErrorOps {
  def this(retrievers: LyricsRetriever*)(implicit c: Configuration) = this(retrievers.toList)

  private implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
  private val logger = c.injector.instance[Logger]
  // TODO better errors when no parser is found
  override def apply(s: Song): Future[Lyrics] =
    retrievers.foldLeft[Future[Lyrics]](retrievers.head.apply(s))((x, y) => x.handleError(e => {
      logger.error("Failed to parse lyrics: ", e)
      y.apply(s)
    }))
}
