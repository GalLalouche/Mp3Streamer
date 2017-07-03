package backend.external.extensions

import backend.external.{MarkedLink, MarkedLinks}
import backend.recon.Reconcilable
import common.rich.RichT._

/** Extenders whose result is not dependent on other links. */
private trait StaticExtender[-R <: Reconcilable] extends LinkExtender[R] {
  protected def apply[T <: R](t: T, e: MarkedLink[T]): Seq[LinkExtension[T]]
  override def apply[T <: R](t: T, e: MarkedLinks[T]): Seq[LinkExtension[T]] =
    e.flatMap(_.opt.filter(_.host == this.host)).ensuring(_.size <= 1).flatMap(apply(t, _)).toList
}
