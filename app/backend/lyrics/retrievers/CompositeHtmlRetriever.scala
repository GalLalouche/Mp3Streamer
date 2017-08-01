package backend.lyrics.retrievers

import backend.Url
import backend.lyrics.Lyrics
import models.Song

import scala.concurrent.{ExecutionContext, Future}

private[lyrics] class CompositeHtmlRetriever(retrievers: List[HtmlRetriever])
    (implicit ec: ExecutionContext) extends CompositeLyricsRetriever(retrievers) with HtmlRetriever {
  def this(retrievers: HtmlRetriever*)(implicit ec: ExecutionContext) = this(retrievers.toList)
  override def doesUrlMatchHost(url: Url): Boolean = {
    retrievers.exists(_ doesUrlMatchHost url)
  }
  override def parse(url: Url, s: Song): Future[Lyrics] = retrievers
      .find(_ doesUrlMatchHost url)
      .fold(Future.failed[Lyrics](new NoSuchElementException(s"No retriever could parse host <${url.host}>"))) {
        r => r.parse(url, s)
      }
}
