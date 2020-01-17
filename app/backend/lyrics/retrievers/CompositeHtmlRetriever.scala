package backend.lyrics.retrievers

import backend.logging.Logger
import backend.lyrics.retrievers.genius.GeniusLyricsRetriever
import javax.inject.Inject

import scala.concurrent.ExecutionContext

private[lyrics] class CompositeHtmlRetriever(
    ec: ExecutionContext, logger: Logger, retrievers: Seq[LyricsRetriever])
    extends CompositeLyricsRetriever(ec, logger, retrievers)
        with HtmlRetriever {
  @Inject() def this(
      ec: ExecutionContext,
      logger: Logger,
      lyricsWikiaRetriever: LyricsWikiaRetriever,
      geniusLyricsRetriever: GeniusLyricsRetriever,
      azLyricsRetriever: AzLyricsRetriever,
      darkLyricsRetriever: DarkLyricsRetriever,
  ) = this(ec, logger, Vector(
    lyricsWikiaRetriever,
    geniusLyricsRetriever,
    azLyricsRetriever,
    darkLyricsRetriever,
  ))
  private implicit val iec: ExecutionContext = ec

  private val htmlRetrievers = retrievers.asInstanceOf[Seq[HtmlRetriever]]
  private val aux = PassiveParser.composite(htmlRetrievers)

  override def doesUrlMatchHost = aux.doesUrlMatchHost
  override val parse = aux.parse
}
