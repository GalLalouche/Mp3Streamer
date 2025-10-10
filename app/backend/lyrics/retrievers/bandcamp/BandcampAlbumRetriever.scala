package backend.lyrics.retrievers.bandcamp

import backend.lyrics.retrievers.{HtmlRetriever, SingleHostParsingHelper}
import backend.lyrics.retrievers.RetrievedLyricsResult.NoLyrics
import com.google.inject.Inject

import scala.concurrent.ExecutionContext

import common.rich.func.kats.ToMoreMonadErrorOps._

private[retrievers] class BandcampAlbumRetriever @Inject() (
    helper: SingleHostParsingHelper,
    externalLinksProvider: BandcampLinksProvider,
    ec: ExecutionContext,
) extends HtmlRetriever {
  private implicit val iec: ExecutionContext = ec

  override val parse = helper(AlbumParser)
  override val doesUrlMatchHost = Utils.doesUrlMatchHost
  override def get = song =>
    externalLinksProvider(song)
      .semiflatMap(parse(_, song))
      .getOrElse(NoLyrics) // Recovering from None
      .orElseFlat(NoLyrics) // Recovering from Future failure
}
