package backend.external.extensions

import backend.external.{Host, MarkedLink}
import backend.recon.Reconcilable

private trait StaticExtender[R <: Reconcilable] {
  def host: Host
  def extend(r: R, e: MarkedLink[R]): Seq[LinkExtension[R]]
}
