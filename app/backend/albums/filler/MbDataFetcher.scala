package backend.albums.filler

import backend.logging.Logger
import backend.mb.MbArtistReconciler
import backend.recon.{Artist, ReconID}
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future, Promise}

import common.concurrency.{DaemonFixedPool, SimpleTypedActor}
import common.rich.RichT._

// Ensures MusicBrainz aren't flooded since:
// 1. A limited number of WS can be used at any given time (semaphores).
// 2. A request for a client has a delay.
@Singleton private class MbDataFetcher @Inject()(
    logger: Logger,
    meta: MbArtistReconciler,
    finisher: AlbumFinisher,
) extends SimpleTypedActor[(Artist, ReconID), Seq[NewAlbumRecon]] {
  private val internet: ExecutionContext = DaemonFixedPool.single(this.simpleName + "-internet")
  private val other: ExecutionContext = DaemonFixedPool(this.simpleName + "-other", 10)
  override def !(m: => (Artist, ReconID)): Future[Seq[NewAlbumRecon]] = {
    val promise = Promise[Seq[NewAlbumRecon]]()
    val (artist, recon) = m
    internet.execute(() => {
      Thread sleep 2000
      logger.verbose(s"Performing fetch for artist <$artist>")
      meta.getAlbumsMetadata(recon)
          .flatMap(finisher.!(artist, _))(other)
          .onComplete(promise.complete)(other)
    })
    promise.future
  }
}
