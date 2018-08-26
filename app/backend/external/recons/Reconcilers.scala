package backend.external.recons

import backend.recon.{Album, Artist, Reconcilable}
import javax.inject.Inject

private[external] case class Reconcilers[R <: Reconcilable](get: Traversable[Reconciler[R]]) {
  def ++(other: Traversable[Reconciler[R]]): Reconcilers[R] = Reconcilers(get ++ other)
  def ++(other: Reconcilers[R]): Reconcilers[R] = ++(other.get)
}
private[external] class ArtistReconcilers @Inject()(lastFmReconciler: LastFmReconciler)
    extends Reconcilers[Artist](Vector(lastFmReconciler))
private[external] class AlbumReconcilers @Inject() extends Reconcilers[Album](Nil)

