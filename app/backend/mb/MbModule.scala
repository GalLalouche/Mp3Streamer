package backend.mb

import backend.recon.{Album, Artist, Reconciler, ReconcilerCacher, ReconID}
import backend.OptionRetriever
import backend.recon.StoredReconResult.{HasReconResult, NoRecon}
import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaPrivateModule

import scala.concurrent.ExecutionContext

import scalaz.OptionT

object MbModule extends ScalaPrivateModule {
  override def configure(): Unit = {
    bind[Reconciler[Album]].to[MbAlbumReconciler]
    expose[Reconciler[Album]]
  }

  @Provides private def artistReconciler(
      artistReconciler: ReconcilerCacher[Artist],
      ec: ExecutionContext
  ): OptionRetriever[Artist, ReconID] = artist => OptionT {
    implicit val iec: ExecutionContext = ec
    artistReconciler(artist).map {
      case NoRecon => None
      case HasReconResult(reconId, _) => Some(reconId)
    }
  }
}
