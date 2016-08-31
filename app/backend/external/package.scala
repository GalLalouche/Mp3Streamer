package backend

import backend.external.extensions.ExtendedLink
import backend.recon.Reconcilable
import backend.Retriever

import scala.concurrent.Future

package object external{
  type Links[R <: Reconcilable] = Traversable[ExternalLink[R]]
  type ExtendedLinks[R <: Reconcilable] = Traversable[ExtendedLink[R]]
  type HostMap[T] = Map[Host, T]
}
