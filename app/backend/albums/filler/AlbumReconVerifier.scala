package backend.albums.filler

import backend.mb.GetReleaseGroupArtists
import backend.recon.{Album, ArtistReconStorage, ReconID, StoredReconResult}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import common.rich.func.RichOptionT.richOptionT

private class AlbumReconVerifier @Inject()(
    artistReconStorage: ArtistReconStorage,
    getReleaseGroupArtists: GetReleaseGroupArtists,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  def apply(artist: Album, id: ReconID): Future[Boolean] = {
    for {
      storedArtistRecon <- artistReconStorage.load(artist.artist).get.map {
        case StoredReconResult.NoRecon =>
          throw new IllegalArgumentException(s"Cannot validate album <$artist> with no reconciled artist")
        case StoredReconResult.HasReconResult(reconId, _) => reconId
      }
      artistsFromRecon <- getReleaseGroupArtists(id)
    } yield artistsFromRecon.map(_._2).contains(storedArtistRecon)
  }
}
