package backend.lyrics.retrievers

import backend.lyrics.Lyrics
import common.rich.func.{MoreFutureInstances, ToMoreFoldableOps}
import models.Song

import scala.concurrent.{ExecutionContext, Future}
import scalaz.std.ListInstances

private[lyrics] class CompositeLyricsRetriever(retrievers: List[LyricsRetriever])(implicit ec: ExecutionContext)
    extends LyricsRetriever
        with MoreFutureInstances with ListInstances with ToMoreFoldableOps {
  override def apply(s: Song): Future[Lyrics] = retrievers.foldMapPE(_ (s))
  def this(retrievers: LyricsRetriever*)(implicit ec: ExecutionContext) = this(retrievers.toList)
}
