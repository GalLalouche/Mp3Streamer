package backend.recon

import backend.Retriever

trait Reconciler[Key <: Reconcilable] extends Retriever[Key, Option[ReconID]]
