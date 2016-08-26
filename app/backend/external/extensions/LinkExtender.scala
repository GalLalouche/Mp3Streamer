package backend.external.extensions

import backend.external.{ExternalLink, LinkExtensions}
import backend.recon.Reconcilable

trait LinkExtender[-R <: Reconcilable] {
  def apply[T <: R](v: ExternalLink[T]): Seq[LinkExtensions[T]]
}
