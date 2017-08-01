package backend.lyrics.retrievers

import backend.lyrics.Lyrics
import common.rich.RichFuture._
import models.Song

import scala.concurrent.{ExecutionContext, Future}

private[lyrics] class CompositeLyricsRetriever(retrievers: List[LyricsRetriever])
    (implicit ec: ExecutionContext) extends LyricsRetriever {
  override def find(s: Song): Future[Lyrics] = {
    // TODO move to somewhere more general
    def first[T](fs: List[() => Future[T]]): Future[T] = fs match {
      case Nil => Future failed new NoSuchElementException
      case x :: xs => x() orElseTry first(xs)
    }
    first(retrievers.map(r => () => r find s))
  }
  def this(retrievers: LyricsRetriever*)(implicit ec: ExecutionContext) = this(retrievers.toList)
}
