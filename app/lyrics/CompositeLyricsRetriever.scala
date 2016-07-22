package lyrics

import common.RichFuture._
import models.Song

import scala.collection.LinearSeq
import scala.concurrent.{ExecutionContext, Future}

private class CompositeLyricsRetriever(retrievers: LinearSeq[LyricsRetriever])(implicit ec: ExecutionContext) extends LyricsRetriever {
  override def apply(s: Song): Future[Lyrics] = {
    def aux(rs: LinearSeq[LyricsRetriever]): Future[Lyrics] = rs match {
      case Nil => Future.failed(new NoSuchElementException("No retriever could find lyrics for " + s))
      case x :: xs => x(s) orElseTry aux(xs)
    }
    aux(retrievers)
  }
  def this(retrievers: LyricsRetriever*)(implicit ec: ExecutionContext) = this(retrievers.toList)
}
