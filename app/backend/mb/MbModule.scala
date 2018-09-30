package backend.mb

import backend.recon.{Album, Artist, Reconciler, ReconcilerCacher, ReconID}
import backend.OptionRetriever
import backend.recon.StoredReconResult.{HasReconResult, NoRecon}
import com.google.inject.Provides
import common.ModuleUtils
import net.codingwell.scalaguice.ScalaPrivateModule

import scala.concurrent.ExecutionContext

object MbModule extends ScalaPrivateModule with ModuleUtils {
  override def configure(): Unit = {
    bind[Reconciler[Album]].to[MbAlbumReconciler]
    expose[Reconciler[Album]]
  }

  @Provides private def albumReconciler(
      artistReconciler: ReconcilerCacher[Artist],
      ec: ExecutionContext
  ): OptionRetriever[Artist, ReconID] = {
    implicit val iec: ExecutionContext = ec
    artistReconciler(_).map {
      case NoRecon => None
      case HasReconResult(reconId, _) => Some(reconId)
    }
  }
}
