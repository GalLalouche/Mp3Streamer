package backend.recon

import backend.mb.MbArtistReconciler
import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

object ReconModule extends ScalaModule {
  @Provides
  private def provideArtistReconcilerCacher(
      artistReconStorage: ArtistReconStorage,
      mbArtistReconciler: MbArtistReconciler,
      ec: ExecutionContext
  ): ReconcilerCacher[Artist] = {
    implicit val iec: ExecutionContext = ec
    new ReconcilerCacher[Artist](artistReconStorage, mbArtistReconciler)
  }

  @Provides
  private def provideAlbumReconcilerCacher(
      artistReconStorage: AlbumReconStorage,
      mbAlbumReconciler: Reconciler[Album],
      ec: ExecutionContext
  ): ReconcilerCacher[Album] = {
    implicit val iec: ExecutionContext = ec
    new ReconcilerCacher[Album](artistReconStorage, mbAlbumReconciler)
  }
}
