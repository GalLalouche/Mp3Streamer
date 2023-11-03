package backend.external.recons

import backend.external.{BaseLink, Host}
import backend.recon.Reconcilable
import backend.OptionRetriever

private[external] trait LinkRetriever[R <: Reconcilable] extends OptionRetriever[R, BaseLink[R]] {
  def host: Host
}
