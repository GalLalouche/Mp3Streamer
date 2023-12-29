package backend.external.recons

import backend.OptionRetriever
import backend.external.{BaseLink, Host}
import backend.recon.Reconcilable

private[external] trait LinkRetriever[R <: Reconcilable] extends OptionRetriever[R, BaseLink[R]] {
  def host: Host
}
