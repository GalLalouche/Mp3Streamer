package backend.albums

import backend.logging.Logger
import backend.mb.MbArtistReconciler
import backend.recon._
import javax.inject.Inject
import models.IOMusicFinder

import scala.concurrent.ExecutionContext

private class NewAlbumsRetrieverFactory @Inject()(
    albumReconStorage: AlbumReconStorage,
    logger: Logger,
    ec: ExecutionContext,
    meta: MbArtistReconciler,
) {
  def apply(reconciler: ReconcilerCacher[Artist], mf: IOMusicFinder) = new NewAlbumsRetriever(
    albumReconStorage: AlbumReconStorage,
    mf: IOMusicFinder,
    logger: Logger,
    ec: ExecutionContext,
    meta: MbArtistReconciler,
    reconciler
  )
}


