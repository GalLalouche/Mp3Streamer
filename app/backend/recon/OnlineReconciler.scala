package backend.recon

import backend.Retriever

trait OnlineReconciler[Key <: Reconcilable] extends Retriever[Key, Option[ReconID]]
