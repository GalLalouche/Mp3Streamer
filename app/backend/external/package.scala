package backend

import backend.external.extensions.ExtendedLink
import backend.recon.{ReconID, Reconcilable}

package object external {
  type BaseLinks[R <: Reconcilable] = Traversable[BaseLink[R]]
  type MarkedLinks[R <: Reconcilable] = Traversable[MarkedLink[R]]
  private[external] type HostMap[T] = Map[Host, T]
  type ExtendedLinks[R <: Reconcilable] = Traversable[ExtendedLink[R]]
  private[external] type ExternalLinkProvider[R <: Reconcilable] = Retriever[ReconID, BaseLinks[R]]
}
