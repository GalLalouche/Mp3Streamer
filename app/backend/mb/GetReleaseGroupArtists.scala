package backend.mb

import com.google.inject.Inject

import backend.recon.{Artist, ReconID}

import scala.concurrent.{ExecutionContext, Future}

class GetReleaseGroupArtists @Inject() (jsonDownloader: JsonDownloader, ec: ExecutionContext) {
  private implicit val iec: ExecutionContext = ec
  def apply(reconID: ReconID): Future[Seq[(Artist, ReconID)]] =
    jsonDownloader("release-group/" + reconID.id, "inc" -> "artists").map(AlbumParser.artistCredits)
}
