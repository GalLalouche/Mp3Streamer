package backend

import backend.external.extensions.ExtendedLink
import backend.recon.{Reconcilable, ReconID}

package object external {
  private[external] type BaseLinks[R <: Reconcilable] = Iterable[BaseLink[R]]
  private[external] type MarkedLinks[R <: Reconcilable] = Iterable[MarkedLink[R]]
  private[external] type HostMap[T] = Map[Host, T]
  private[external] type ExtendedLinks[R <: Reconcilable] = Iterable[ExtendedLink[R]]
  private[external] type ExternalLinkProvider[R <: Reconcilable] = Retriever[ReconID, BaseLinks[R]]
}
