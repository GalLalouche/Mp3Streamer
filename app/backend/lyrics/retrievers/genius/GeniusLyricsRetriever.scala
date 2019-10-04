package backend.lyrics.retrievers.genius

import backend.lyrics.retrievers.{HtmlRetriever, SingleHostParsingHelper}
import backend.lyrics.retrievers.RetrievedLyricsResult.NoLyrics
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.option.optionInstance
import common.rich.func.ToMoreFoldableOps._

private[lyrics] class GeniusLyricsRetriever @Inject()(
    singleHostHelper: SingleHostParsingHelper,
    api: API,
    ec: ExecutionContext
) extends HtmlRetriever {
  private implicit val iec: ExecutionContext = ec

  override val parse = singleHostHelper(LyricsParser)
  override val doesUrlMatchHost = _.address startsWith "https://genius.com"
  override val get = song => api.getLyricUrl(song).flatMap(_.mapHeadOrElse(
    f = parse(_, song),
    default = Future.successful(NoLyrics))
  )
}
