package backend.external.recons

import backend.configs.Configuration
import backend.recon.{Album, Artist}

private[external] object Reconcilers {
  def artist(implicit c: Configuration): Traversable[Reconciler[Artist]] = List(new LastFmReconciler())
  def album(implicit c: Configuration): Traversable[Reconciler[Album]] = Nil
}
