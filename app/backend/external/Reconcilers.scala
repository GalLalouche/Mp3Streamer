package backend.external

import backend.recon.{Album, Artist}
import common.io.InternetTalker

import scala.concurrent.ExecutionContext

object Reconcilers {
  def artist(implicit ec: ExecutionContext, it: InternetTalker): Traversable[Reconciler[Artist]] = List(new LastFmReconciler())
  def album(implicit ec: ExecutionContext, it: InternetTalker): Traversable[Reconciler[Album]] = Nil
}
