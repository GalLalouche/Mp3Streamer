package backend.external.mark

import backend.external.{Host, LinkMark, MarkedLink}
import backend.recon.Reconcilable

import scala.concurrent.Future

private[external] trait ExternalLinkMarker[R <: Reconcilable] {
  def host: Host
  def apply(l: MarkedLink[R]): Future[LinkMark]
}
