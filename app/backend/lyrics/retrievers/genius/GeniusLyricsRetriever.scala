package backend.lyrics.retrievers.genius

import backend.lyrics.retrievers.{HtmlRetriever, SingleHostParsingHelper}
import backend.lyrics.retrievers.RetrievedLyricsResult.NoLyrics
import com.google.inject.Inject

import scala.concurrent.ExecutionContext

import common.rich.func.BetterFutureInstances._

private[lyrics] class GeniusLyricsRetriever @Inject() (
    singleHostHelper: SingleHostParsingHelper,
    api: API,
    ec: ExecutionContext,
) extends HtmlRetriever {
  private implicit val iec: ExecutionContext = ec

  override val parse = singleHostHelper(LyricsParser)
  override val doesUrlMatchHost = _.toStringPunycode.startsWith("https://genius.com")
  override val get = song => api.getLyricUrl(song).mapF(parse(_, song)) | NoLyrics
}
