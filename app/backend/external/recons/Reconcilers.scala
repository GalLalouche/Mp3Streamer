package backend.external.recons

import backend.recon.{Album, Artist}
import common.io.InternetTalker

import scala.concurrent.ExecutionContext

private[external] object Reconcilers {
  def artist(implicit ec: ExecutionContext, it: InternetTalker): Traversable[Reconciler[Artist]] = List(new LastFmReconciler())
  def album(implicit ec: ExecutionContext, it: InternetTalker): Traversable[Reconciler[Album]] = Nil
}
