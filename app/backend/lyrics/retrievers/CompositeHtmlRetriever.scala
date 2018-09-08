package backend.lyrics.retrievers

import backend.Url
import backend.logging.Logger
import common.rich.func.ToMoreFoldableOps
import javax.inject.Inject
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.OptionInstances

private[lyrics] class CompositeHtmlRetriever(
    ec: ExecutionContext, logger: Logger, retrievers: Seq[LyricsRetriever])
    extends CompositeLyricsRetriever(ec, logger, retrievers)
        with HtmlRetriever with ToMoreFoldableOps with OptionInstances {
  @Inject() def this(
      ec: ExecutionContext,
      logger: Logger,
      lyricsWikiaRetriever: LyricsWikiaRetriever,
      darkLyricsRetriever: DarkLyricsRetriever,
      azLyricsRetriever: AzLyricsRetriever,
      geniusLyricsRetriever: GeniusLyricsRetriever,
  ) = this(ec, logger, Vector(
    lyricsWikiaRetriever,
    darkLyricsRetriever,
    azLyricsRetriever,
    geniusLyricsRetriever,
  ))
  private implicit val iec: ExecutionContext = ec

  private val htmlRetrievers = retrievers.asInstanceOf[Seq[HtmlRetriever]]

  override def doesUrlMatchHost = url => htmlRetrievers.exists(_ doesUrlMatchHost url)
  override val parse = (url: Url, s: Song) => htmlRetrievers
      .find(_ doesUrlMatchHost url)
      .mapHeadOrElse(_.parse(url, s),
        Future failed new NoSuchElementException(s"No retriever could parse host <${url.host}>"))
}
