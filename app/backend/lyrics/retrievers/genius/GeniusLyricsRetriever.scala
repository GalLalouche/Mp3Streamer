package backend.lyrics.retrievers.genius

import backend.lyrics.retrievers.{AmphiRetriever, SingleHostParsingHelper}
import backend.lyrics.retrievers.RetrievedLyricsResult.NoLyrics
import com.google.inject.Inject

import scala.concurrent.ExecutionContext

import common.rich.func.kats.RichOptionT.richOptionT

private[retrievers] class GeniusLyricsRetriever @Inject() (
    singleHostHelper: SingleHostParsingHelper,
    api: API,
    ec: ExecutionContext,
) extends AmphiRetriever {
  private implicit val iec: ExecutionContext = ec

  override val parse = singleHostHelper(LyricsParser)
  override val doesUrlMatchHost = _.toStringPunycode.startsWith("https://genius.com")
  override val get = song => api.getLyricUrl(song).semiflatMap(parse(_, song)) | NoLyrics
}
