package backend.lyrics.retrievers.bandcamp

import backend.logging.Logger
import backend.lyrics.retrievers.{HtmlRetriever, SingleHostParsingHelper}
import common.rich.func.ToMoreMonadErrorOps._
import backend.lyrics.retrievers.RetrievedLyricsResult.NoLyrics
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.option.optionInstance
import scalaz.std.scalaFuture.futureInstance
import common.rich.func.ToMoreFoldableOps._

private[retrievers] class BandcampAlbumRetriever @Inject()(
    helper: SingleHostParsingHelper,
    externalLinksProvider: BandcampLinksProvider,
    logger: Logger,
    ec: ExecutionContext,
) extends HtmlRetriever {
  private implicit val iec: ExecutionContext = ec

  override val parse = helper(AlbumParser)
  override val doesUrlMatchHost = Utils.doesUrlMatchHost
  override def get = song =>
    externalLinksProvider(song)
        .flatMap(_.mapHeadOrElse(parse(_, song), Future.successful(NoLyrics)))
        .orElse(NoLyrics)
}
