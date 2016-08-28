package backend.external.extensions

import backend.external.{ExternalLink, LinkExtension}
import backend.recon.Reconcilable

private trait LinkExtender[-R <: Reconcilable] {
  protected def append[T <: R](e: ExternalLink[T], suffixes: (String, String)*): Seq[LinkExtension[T]] =
    suffixes.map(x => x._1 -> (e.link + "/" + x._2)).map(e => LinkExtension[T](e._1, e._2))
  def apply[T <: R](v: ExternalLink[T]): Seq[LinkExtension[T]]
}
