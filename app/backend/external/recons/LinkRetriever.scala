package backend.external.recons

import backend.Retriever
import backend.external.{BaseLink, Host}
import backend.recon.Reconcilable

private[external] trait LinkRetriever[R <: Reconcilable] extends Retriever[R, Option[BaseLink[R]]] {
  def host: Host
}
