package backend.recon

import backend.mb.MbArtistReconciler
import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

object ReconModule extends ScalaModule {
  @Provides private def artistReconcilerCacher(
      artistReconStorage: ArtistReconStorage,
      mbArtistReconciler: MbArtistReconciler,
      ec: ExecutionContext
  ): ReconcilerCacher[Artist] = new ReconcilerCacher[Artist](artistReconStorage, mbArtistReconciler)(ec)

  @Provides private def albumReconcilerCacher(
      artistReconStorage: AlbumReconStorage,
      mbAlbumReconciler: Reconciler[Album],
      ec: ExecutionContext
  ): ReconcilerCacher[Album] = new ReconcilerCacher[Album](artistReconStorage, mbAlbumReconciler)(ec)
}
