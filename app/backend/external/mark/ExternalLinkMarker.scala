package backend.external.mark

import scala.concurrent.Future

import backend.external.{Host, LinkMark, MarkedLink}
import backend.recon.Reconcilable

private[external] trait ExternalLinkMarker[R <: Reconcilable] {
  def host: Host
  def apply(l: MarkedLink[R]): Future[LinkMark]
}
