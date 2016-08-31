package backend

import backend.external.extensions.ExtendedLink
import backend.recon.{ReconID, Reconcilable}

package object external{
  type Links[R <: Reconcilable] = Traversable[ExternalLink[R]]
  type ExtendedLinks[R <: Reconcilable] = Traversable[ExtendedLink[R]]
  type HostMap[T] = Map[Host, T]
  type ExternalLinkProvider[R <: Reconcilable] = Retriever[ReconID, Links[R]]
}
