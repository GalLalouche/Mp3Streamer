package backend.external.recons

import backend.recon.{Album, Artist}
import common.io.InternetTalker

private[external] object Reconcilers {
  def artist(implicit it: InternetTalker): Traversable[Reconciler[Artist]] = List(new LastFmReconciler())
  def album(implicit it: InternetTalker): Traversable[Reconciler[Album]] = Nil
}
