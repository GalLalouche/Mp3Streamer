package backend.mb

import backend.recon.{Album, Reconciler}
import common.ModuleUtils
import net.codingwell.scalaguice.ScalaModule

object MbModule extends ScalaModule with ModuleUtils {
  override def configure(): Unit = {
    install[Reconciler[Album], MbAlbumReconciler, AlbumReconcilerFactory]
  }
}
