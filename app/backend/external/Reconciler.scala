package backend.external

import backend.recon.Reconcilable
import backend.storage.Retriever

trait Reconciler[R <: Reconcilable] extends Retriever[R, Option[ExternalLink[R]]]
