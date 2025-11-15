package backend.mb

import backend.recon.ReconID
import com.google.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

private[backend] class AlbumFetcher @Inject() (
    ec: ExecutionContext,
    downloader: JsonDownloader,
) {
  private implicit val iec: ExecutionContext = ec

  def getAlbumsMetadata(artistKey: ReconID): Future[Seq[AlbumMetadata]] =
    downloader("release-group", "artist" -> artistKey.id).map(AlbumParser.releaseGroups)
}
