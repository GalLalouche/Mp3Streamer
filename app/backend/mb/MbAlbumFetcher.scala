package backend.mb

import backend.recon.ReconID
import com.google.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

private[backend] class MbAlbumFetcher @Inject() (
    ec: ExecutionContext,
    downloader: JsonDownloader,
) {
  private implicit val iec: ExecutionContext = ec

  def getAlbumsMetadata(artistKey: ReconID): Future[Seq[MbAlbumMetadata]] =
    downloader("release-group", "artist" -> artistKey.id).map(AlbumParser.releaseGroups)
}
