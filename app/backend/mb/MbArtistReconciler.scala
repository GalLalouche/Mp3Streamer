package backend.mb

import backend.RichTime.OrderingLocalDate
import backend.recon.{Artist, Reconciler, ReconID}
import backend.FutureOption
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.scalaFuture.futureInstance
import common.rich.func.ToMoreMonadErrorOps._

import common.json.RichJson._

class MbArtistReconciler @Inject()(
    ec: ExecutionContext,
    parser: AlbumParser,
    downloader: JsonDownloader,
) extends Reconciler[Artist] {
  private implicit val iec: ExecutionContext = ec
  override def apply(a: Artist): FutureOption[ReconID] =
    downloader("artist/", "query" -> a.name)
        .map(_.objects("artists").maxBy(_ int "score"))
        .filterWithMessage(_.int("score") == 100, "could not find a 100 match")
        .map(_ ostr "id" map ReconID)

  def getAlbumsMetadata(artistKey: ReconID): Future[Seq[MbAlbumMetadata]] =
    downloader("release-group", "artist" -> artistKey.id, "limit" -> "100")
        .map(_.objects("release-groups")
            .flatMap(parser.apply)
            .sortBy(_.releaseDate)
        )
}
