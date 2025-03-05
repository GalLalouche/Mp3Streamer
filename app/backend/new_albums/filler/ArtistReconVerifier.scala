package backend.new_albums.filler

import backend.mb.{MbAlbumMetadata, MbArtistReconciler}
import backend.recon.{Album, Artist, ReconID, StringReconScorer}
import com.google.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import common.rich.primitives.RichBoolean._

private class ArtistReconVerifier @Inject() (
    ea: ExistingAlbums,
    reconciler: MbArtistReconciler,
    ec: ExecutionContext,
    stringReconScorer: StringReconScorer,
) {
  private implicit val iec: ExecutionContext = ec
  def apply(artist: Artist, id: ReconID): Future[Boolean] = reconciler
    .getAlbumsMetadata(id)
    .map(intersects(ea.albums(artist)))

  private def intersects(album: Set[Album])(reconAlbums: Seq[MbAlbumMetadata]): Boolean = {
    val albumTitles = album.map(_.title)
    val $ =
      reconAlbums.view.map(_.title).exists(t => albumTitles.map(stringReconScorer(_, t)).max > 0.9)
    if ($.isFalse)
      scribe.debug(s"Could not verify artist recon <${album.head.artist}>")
    $
  }
}
