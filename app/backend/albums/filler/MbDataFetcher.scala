package backend.albums.filler

import backend.logging.Logger
import backend.mb.MbArtistReconciler
import backend.recon.{Artist, ReconID}
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

import common.concurrency.DaemonFixedPool
import common.rich.RichT._

@Singleton private class MbDataFetcher @Inject()(
    logger: Logger,
    meta: MbArtistReconciler,
    ea: EagerExistingAlbums,
) extends {
  private val ec: ExecutionContext = DaemonFixedPool(this.simpleName, 10)
  def apply(artist: Artist, reconID: ReconID): Future[Seq[NewAlbumRecon]] = {
    logger.verbose(s"Performing fetch for artist <$artist>")
    meta.getAlbumsMetadata(reconID)
        .map(ea.removeExistingAlbums(artist, _))(ec)
  }
}
