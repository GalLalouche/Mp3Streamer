package backend

import backend.external.extensions.ExtendedLink
import backend.recon.Reconcilable
import backend.Retriever

package object external{
  type Reconciler[R <: Reconcilable] = Retriever[R, Option[ExternalLink[R]]]
  type Links[R <: Reconcilable] = Traversable[ExternalLink[R]]
  type ExtendedLinks[R <: Reconcilable] = Traversable[ExtendedLink[R]]
}
