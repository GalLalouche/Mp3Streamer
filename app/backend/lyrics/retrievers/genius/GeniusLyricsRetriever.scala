package backend.lyrics.retrievers.genius

import javax.inject.Inject
import scala.concurrent.ExecutionContext

import backend.lyrics.retrievers.{HtmlRetriever, SingleHostParsingHelper}
import backend.lyrics.retrievers.RetrievedLyricsResult.NoLyrics
import common.rich.func.BetterFutureInstances._

private[lyrics] class GeniusLyricsRetriever @Inject() (
    singleHostHelper: SingleHostParsingHelper,
    api: API,
    ec: ExecutionContext,
) extends HtmlRetriever {
  private implicit val iec: ExecutionContext = ec

  override val parse = singleHostHelper(LyricsParser)
  override val doesUrlMatchHost = _.address.startsWith("https://genius.com")
  override val get = song => api.getLyricUrl(song).flatMapF(parse(_, song)) | NoLyrics
}
