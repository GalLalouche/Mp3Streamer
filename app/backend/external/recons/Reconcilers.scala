package backend.external.recons

import backend.configs.Configuration
import backend.recon.{Album, Artist}
import net.codingwell.scalaguice.InjectorExtensions._

private[external] object Reconcilers {
  def artist(implicit c: Configuration): Traversable[Reconciler[Artist]] =
    List(c.injector.instance[LastFmReconciler])
  def album: Traversable[Reconciler[Album]] = Nil
}
