package backend.recon

import backend.OptionRetriever

trait Reconciler[Key <: Reconcilable] extends OptionRetriever[Key, ReconID]
