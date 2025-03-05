package backend.lyrics.retrievers

import backend.lyrics.retrievers.genius.GeniusLyricsRetriever
import com.google.inject.Inject

import scala.concurrent.ExecutionContext

private[lyrics] class CompositeHtmlRetriever(
    ec: ExecutionContext,
    retrievers: Seq[HtmlRetriever],
) extends CompositeLyricsRetriever(retrievers)(ec)
    with HtmlRetriever {
  @Inject() def this(
      ec: ExecutionContext,
      geniusLyricsRetriever: GeniusLyricsRetriever,
      azLyricsRetriever: AzLyricsRetriever,
      darkLyricsRetriever: DarkLyricsRetriever,
  ) = this(
    ec,
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
