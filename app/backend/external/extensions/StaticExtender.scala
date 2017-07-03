package backend.external.extensions

import backend.external.{MarkedLink, MarkedLinks}
import backend.recon.Reconcilable
import common.rich.RichT._

/** Extenders whose result is not dependent on other links. */
private trait StaticExtender[R <: Reconcilable] extends LinkExtender[R] {
  protected def apply(t: R, e: MarkedLink[R]): Seq[LinkExtension[R]]
  override def apply(t: R, e: MarkedLinks[R]): Seq[LinkExtension[R]] =
    e.flatMap(_.opt.filter(_.host == this.host)).ensuring(_.size <= 1).flatMap(apply(t, _)).toList
}
