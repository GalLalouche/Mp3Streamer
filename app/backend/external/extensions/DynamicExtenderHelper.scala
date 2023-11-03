package backend.external.extensions

import backend.external.MarkedLinks
import backend.recon.Reconcilable

/** Helper for extenders whose result is dependent on other links. */
private object DynamicExtenderHelper {
  def apply[R <: Reconcilable](
      extender: DynamicExtender[R],
  )(r: R, e: MarkedLinks[R]): Seq[LinkExtension[R]] = {
    val (linkToModify, otherLinks) = e.partition(_.host == extender.host).ensuring(_._1.size <= 1)
    linkToModify.toVector.flatMap(extender.extend(r, _, otherLinks))
  }
}
