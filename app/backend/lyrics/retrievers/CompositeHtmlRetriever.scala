package backend.lyrics.retrievers

import backend.Url
import backend.configs.Configuration
import backend.lyrics.Lyrics
import common.rich.func.ToMoreFoldableOps
import models.Song

import scala.concurrent.Future

import scalaz.std.OptionInstances

private[lyrics] class CompositeHtmlRetriever(retrievers: List[HtmlRetriever])(implicit c: Configuration)
    extends CompositeLyricsRetriever(retrievers) with HtmlRetriever
        with ToMoreFoldableOps with OptionInstances {
  def this(retrievers: HtmlRetriever*)(implicit c: Configuration) = this(retrievers.toList)

  override def doesUrlMatchHost(url: Url): Boolean = retrievers.exists(_ doesUrlMatchHost url)
  override def parse(url: Url, s: Song): Future[Lyrics] = retrievers
      .find(_ doesUrlMatchHost url)
      .mapHeadOrElse(_.parse(url, s),
        Future failed new NoSuchElementException(s"No retriever could parse host <${url.host}>"))
}
