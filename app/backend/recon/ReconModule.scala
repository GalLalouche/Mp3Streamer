package backend.recon

import backend.albums.filler.VerifiedMbArtistReconciler
import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

object ReconModule extends ScalaModule {
  override def configure(): Unit = {
    bind[ArtistReconStorage].to[SlickArtistReconStorage]
    bind[AlbumReconStorage].to[SlickAlbumReconStorage]
  }
  @Provides private def artistReconcilerCacher(
      artistReconStorage: ArtistReconStorage,
      mbArtistReconciler: VerifiedMbArtistReconciler,
      ec: ExecutionContext,
  ): ReconcilerCacher[Artist] =
    new ReconcilerCacher[Artist](artistReconStorage, mbArtistReconciler)(ec)

  @Provides private def albumReconcilerCacher(
      artistReconStorage: AlbumReconStorage,
      mbAlbumReconciler: Reconciler[Album],
      ec: ExecutionContext,
  ): ReconcilerCacher[Album] =
    new ReconcilerCacher[Album](artistReconStorage, mbAlbumReconciler)(ec)
}
