package backend.albums.filler

import backend.mb.{MbAlbumMetadata, MbArtistReconciler}
import backend.recon.{Album, Artist, ReconID, StringReconScorer}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import common.rich.primitives.RichBoolean._

private class ArtistReconVerifier @Inject()(
    ea: ExistingAlbums,
    reconciler: MbArtistReconciler,
    ec: ExecutionContext
) {
  private implicit val iec: ExecutionContext = ec
  def apply(artist: Artist, id: ReconID): Future[Boolean] = reconciler.getAlbumsMetadata(id)
      .map(ArtistReconVerifier.intersects(ea.albums(artist)))
}

private object ArtistReconVerifier {
  private def intersects(album: Set[Album])(reconAlbums: Seq[MbAlbumMetadata]): Boolean = {
    val albumTitles = album.map(_.title)
    val $ = reconAlbums.view.map(_.title).exists(t => albumTitles.map(StringReconScorer(_, t)).max > 0.9)
    if ($.isFalse)
      println(s"Could not reconcile ${album.head.artist}")
    $
  }
}
