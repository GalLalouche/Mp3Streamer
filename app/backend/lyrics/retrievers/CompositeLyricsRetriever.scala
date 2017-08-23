package backend.lyrics.retrievers

import backend.lyrics.Lyrics
import common.rich.RichFuture._
import models.Song

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Monoid
import scalaz.std.{FutureInstances, ListInstances}
import scalaz.syntax.ToFoldableOps

private[lyrics] class CompositeLyricsRetriever(retrievers: List[LyricsRetriever])(implicit ec: ExecutionContext)
    extends LyricsRetriever
        with FutureInstances with ListInstances with ToFoldableOps {
  // TODO move to common
  private implicit def FutureMonoid[T]: Monoid[Future[T]] = new Monoid[Future[T]] {
    override def zero: Future[T] = Future failed new NoSuchElementException
    override def append(f1: Future[T], f2: => Future[T]): Future[T] = f1 orElseTry f2
  }
  override def apply(s: Song): Future[Lyrics] = retrievers.foldMap(_ (s))
  def this(retrievers: LyricsRetriever*)(implicit ec: ExecutionContext) = this(retrievers.toList)
}
