package backend.external.extensions

import backend.external.{Host, MarkedLink, MarkedLinks}
import backend.recon.Reconcilable

private trait DynamicExtender[R <: Reconcilable] {
  def host: Host
  def extend(r: R, linkToExtend: MarkedLink[R], otherLinks: MarkedLinks[R]): Seq[LinkExtension[R]]
}
