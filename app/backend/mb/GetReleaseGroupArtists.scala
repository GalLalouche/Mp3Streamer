package backend.mb

import backend.recon.{Artist, ReconID}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class GetReleaseGroupArtists @Inject()(
    jsonDownloader: JsonDownloader,
    albumParser: AlbumParser,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  def apply(reconID: ReconID): Future[Seq[(Artist, ReconID)]] =
    jsonDownloader("release-group/" + reconID.id, "inc" -> "artists").map(albumParser.artistCredits)
}
