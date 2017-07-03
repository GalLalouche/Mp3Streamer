package backend.external.extensions

import backend.external.{MarkedLink, MarkedLinks}
import backend.recon.Reconcilable

/** Extenders whose result is dependent on other links. */
private trait DynamicExtender[R <: Reconcilable] extends LinkExtender[R] {
  protected def apply(t: R, linkToModify: MarkedLink[R], otherLinks: MarkedLinks[R]): Seq[LinkExtension[R]]
  override def apply(t: R, e: MarkedLinks[R]): Seq[LinkExtension[R]] = {
    val (linkToModify, otherLinks) = e.partition(_.host == this.host).ensuring(_._1.size <= 1)
    linkToModify.toList.flatMap(apply(t, _, otherLinks))
  }
}
