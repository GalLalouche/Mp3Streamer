package backend.external

import backend.recon.{ReconID, Reconcilable}
import backend.Retriever

trait ExternalLinkProvider[T <: Reconcilable] extends Retriever[ReconID, Links[T]]

