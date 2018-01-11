package backend.lyrics.retrievers

import backend.lyrics.Lyrics
import common.rich.RichT._
import models.Song

import scala.concurrent.{ExecutionContext, Future}
import scalaz.std.{FutureInstances, ListInstances}
import scalaz.syntax.ToMonadErrorOps

private[lyrics] class CompositeLyricsRetriever(retrievers: List[LyricsRetriever])(implicit ec: ExecutionContext)
    extends LyricsRetriever
        with FutureInstances with ListInstances with ToMonadErrorOps {
  override def apply(s: Song): Future[Lyrics] =
    retrievers.foldLeft[Future[Lyrics]](retrievers.head.apply(s))((x, y) => x.handleError(y.apply(s).const))
  def this(retrievers: LyricsRetriever*)(implicit ec: ExecutionContext) = this(retrievers.toList)
}
