package backend.mb

import scala.concurrent.ExecutionContext
import scalaz.OptionT

import backend.recon.{Album, Artist, ReconID, Reconciler, ReconcilerCacher}
import backend.recon.StoredReconResult.{HasReconResult, NoRecon}
import backend.OptionRetriever
import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaPrivateModule

object MbModule extends ScalaPrivateModule {
  override def configure(): Unit = {
    bind[Reconciler[Album]].to[MbAlbumReconciler]
    expose[Reconciler[Album]]
  }

  @Provides private def artistReconciler(
      artistReconciler: ReconcilerCacher[Artist],
      ec: ExecutionContext,
  ): OptionRetriever[Artist, ReconID] = artist =>
    OptionT {
      implicit val iec: ExecutionContext = ec
      artistReconciler(artist).map {
        case NoRecon => None
        case HasReconResult(reconId, _) => Some(reconId)
      }
    }
}
