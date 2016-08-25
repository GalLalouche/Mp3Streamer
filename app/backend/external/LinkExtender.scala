package backend.external

import backend.recon.Reconcilable

trait LinkExtender[-R <: Reconcilable] {
  def apply[T <: R](v: ExternalLink[T]): Seq[LinkExtensions[T]]
}
