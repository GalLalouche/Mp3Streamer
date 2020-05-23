package backend.albums.filler

import backend.logging.Logger
import backend.mb.MbArtistReconciler
import backend.recon.{Artist, ReconID}
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

import common.concurrency.{DaemonFixedPool, SimpleTypedActor}
import common.rich.RichT._

@Singleton private class MbDataFetcher @Inject()(
    logger: Logger,
    meta: MbArtistReconciler,
    finisher: AlbumFinisher,
) extends SimpleTypedActor[(Artist, ReconID), Seq[NewAlbumRecon]] {
  private val ec: ExecutionContext = DaemonFixedPool(this.simpleName, 10)
  override def !(m: => (Artist, ReconID)): Future[Seq[NewAlbumRecon]] = {
    val (artist, recon) = m
    logger.verbose(s"Performing fetch for artist <$artist>")
    meta.getAlbumsMetadata(recon).flatMap(finisher(artist, _))(ec)
  }
}
