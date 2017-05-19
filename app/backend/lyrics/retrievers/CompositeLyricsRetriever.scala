package backend.lyrics.retrievers

import backend.Url
import backend.lyrics.Lyrics
import models.Song

import scala.collection.LinearSeq
import common.rich.RichFuture._

import scala.concurrent.{ExecutionContext, Future}

private[lyrics] class CompositeLyricsRetriever(retrievers: LinearSeq[LyricsRetriever])(implicit ec: ExecutionContext) extends LyricsRetriever {
  // TODO extract
  override def find(s: Song): Future[Lyrics] = {
    def aux(rs: LinearSeq[LyricsRetriever]): Future[Lyrics] = rs match {
      case Nil => Future.failed(new NoSuchElementException("No retriever could find lyrics for " + s))
      case x :: xs => x.find(s) orElseTry aux(xs)
    }
    aux(retrievers)
  }
  def this(retrievers: LyricsRetriever*)(implicit ec: ExecutionContext) = this(retrievers.toList)
  override def doesUrlMatchHost(url: Url): Boolean = {
    retrievers.exists(_ doesUrlMatchHost url)
  }
  override def parse(url: Url, s: Song): Future[Lyrics] =
    retrievers.find(_ doesUrlMatchHost url)
      .fold(Future.failed[Lyrics](new NoSuchElementException(s"No retriever could parse host <${url.host}>"))) {
        r => r.parse(url, s)
      }
}
