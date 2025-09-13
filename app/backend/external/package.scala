package backend

import backend.external.extensions.ExtendedLink
import backend.recon.{Reconcilable, ReconID}

package object external {
  private[external] type BaseLinks[R <: Reconcilable] = Traversable[BaseLink[R]]
  private[external] type MarkedLinks[R <: Reconcilable] = Traversable[MarkedLink[R]]
  private[external] type HostMap[T] = Map[Host, T]
  private[external] type ExtendedLinks[R <: Reconcilable] = Traversable[ExtendedLink[R]]
  private[external] type ExternalLinkProvider[R <: Reconcilable] = Retriever[ReconID, BaseLinks[R]]
}
