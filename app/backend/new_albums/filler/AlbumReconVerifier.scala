package backend.new_albums.filler

import backend.mb.GetReleaseGroupArtists
import backend.recon.{Album, ArtistReconStorage, ReconID, StoredReconResult}
import com.google.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.kats.RichOptionT.richOptionT

private class AlbumReconVerifier @Inject() (
    artistReconStorage: ArtistReconStorage,
    getReleaseGroupArtists: GetReleaseGroupArtists,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  def apply(artist: Album, id: ReconID): Future[Boolean] =
    for {
      storedArtistRecon <- artistReconStorage.load(artist.artist).get.map {
        case StoredReconResult.StoredNull =>
          throw new IllegalArgumentException(
            s"Cannot validate album <$artist> with no reconciled artist",
          )
        case StoredReconResult.HasReconResult(reconId, _) => reconId
      }
      artistsFromRecon <- getReleaseGroupArtists(id)
    } yield artistsFromRecon.map(_._2).contains(storedArtistRecon)
}
