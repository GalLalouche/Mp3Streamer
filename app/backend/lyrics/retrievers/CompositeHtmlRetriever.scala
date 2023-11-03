package backend.lyrics.retrievers

import backend.logging.Logger
import backend.lyrics.retrievers.genius.GeniusLyricsRetriever
import javax.inject.Inject

import scala.concurrent.ExecutionContext

private[lyrics] class CompositeHtmlRetriever(
    ec: ExecutionContext,
    logger: Logger,
    retrievers: Seq[HtmlRetriever],
) extends CompositeLyricsRetriever(logger, retrievers)(ec)
    with HtmlRetriever {
  @Inject() def this(
      ec: ExecutionContext,
      logger: Logger,
      geniusLyricsRetriever: GeniusLyricsRetriever,
      azLyricsRetriever: AzLyricsRetriever,
      darkLyricsRetriever: DarkLyricsRetriever,
  ) = this(
    ec,
    logger,
    Vector(
      geniusLyricsRetriever,
      azLyricsRetriever,
      darkLyricsRetriever,
    ),
  )
  private implicit val iec: ExecutionContext = ec

  private val aux = PassiveParser.composite(retrievers: _*)

  override def doesUrlMatchHost = aux.doesUrlMatchHost
  override val parse = aux.parse
}
