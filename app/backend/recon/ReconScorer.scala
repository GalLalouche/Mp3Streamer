package backend.recon

trait ReconScorer[T <: Reconcilable] extends ((T, T) => Double)
