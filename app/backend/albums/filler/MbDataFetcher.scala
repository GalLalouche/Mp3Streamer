package backend.albums.filler

import backend.logging.Logger
import backend.mb.MbArtistReconciler
import backend.recon.{Artist, ReconID}
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

import common.concurrency.{DaemonFixedPool, SimpleTypedActor}
import common.rich.RichT._

// Ensures MusicBrainz aren't flooded since:
// 1. A limited number of WS can be used at any given time (single threaded actors).
// 2. A request for a client has a delay.
@Singleton private class MbDataFetcher @Inject()(
    logger: Logger,
    meta: MbArtistReconciler,
    finisher: AlbumFinisher,
) extends SimpleTypedActor[(Artist, ReconID), Seq[NewAlbumRecon]] {
  private val other: ExecutionContext = DaemonFixedPool(this.simpleName + "-other", 10)
  private val internet = SimpleTypedActor.async[(Artist, ReconID), Seq[NewAlbumRecon]](
    this.simpleName + "-internet", {case (artist: Artist, recon: ReconID) =>
      Thread sleep 2000
      logger.verbose(s"Performing fetch for artist <$artist>")
      meta.getAlbumsMetadata(recon).flatMap(finisher(artist, _))(other)
    })
  override def !(m: => (Artist, ReconID)): Future[Seq[NewAlbumRecon]] = internet ! m
}
