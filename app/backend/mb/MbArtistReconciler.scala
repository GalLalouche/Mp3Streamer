package backend.mb

import backend.recon.{Artist, Reconciler, ReconID}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import scalaz.OptionT
import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreMonadErrorOps._

import common.json.RichJson._

private[backend] class MbArtistReconciler @Inject()(
    ec: ExecutionContext,
    parser: AlbumParser,
    downloader: JsonDownloader,
) extends Reconciler[Artist] {
  private implicit val iec: ExecutionContext = ec
  override def apply(a: Artist) = OptionT {
    downloader("artist/", "query" -> a.name)
        .map(_.objects("artists"))
        .filterWithMessage(_.nonEmpty, s"Found no artists for <$a>")
        .map(_.maxBy(_ int "score"))
        .filterWithMessage(_.int("score") == 100, "could not find a 100 match")
        .map(_ ostr "id" map ReconID.validateOrThrow)
  }

  def getAlbumsMetadata(artistKey: ReconID): Future[Seq[MbAlbumMetadata]] =
  // Starting from releases actually filters out all the "Sampler/Promo" crap.
    downloader("release", "artist" -> artistKey.id, "limit" -> "10000", "inc" -> "release-groups")
        .map(parser.releaseToReleaseGroups)
}
