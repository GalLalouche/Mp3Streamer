package backend.lyrics.retrievers

import backend.Url
import backend.lyrics.Lyrics
import common.rich.primitives.RichOption._
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
      .mapOrElse(_.parse(url, s), Future failed new NoSuchElementException(s"No retriever could parse host <${url.host}>"))
}
