package backend.lyrics.retrievers.bandcamp

import backend.lyrics.retrievers.{HtmlRetriever, SingleHostParsingHelper}
import backend.lyrics.retrievers.RetrievedLyricsResult.NoLyrics
import javax.inject.Inject

import scala.concurrent.ExecutionContext

import scalaz.std.scalaFuture.futureInstance
import common.rich.func.ToMoreMonadErrorOps._

private[retrievers] class BandcampAlbumRetriever @Inject()(
    helper: SingleHostParsingHelper,
    externalLinksProvider: BandcampLinksProvider,
    ec: ExecutionContext,
) extends HtmlRetriever {
  private implicit val iec: ExecutionContext = ec

  override val parse = helper(AlbumParser)
  override val doesUrlMatchHost = Utils.doesUrlMatchHost
  override def get = song =>
    externalLinksProvider(song).flatMapF(parse(_, song))
        .getOrElse(NoLyrics) // Recovering from None
        .orElse(NoLyrics) // Recovering from Future failure
}
