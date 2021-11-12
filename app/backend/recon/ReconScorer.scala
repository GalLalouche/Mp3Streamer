package backend.recon

import javax.inject.Inject

trait ReconScorer[T <: Reconcilable] extends ((T, T) => Double)

object ReconScorers {
}
