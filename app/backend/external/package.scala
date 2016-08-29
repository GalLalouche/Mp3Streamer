package backend

import backend.recon.Reconcilable
import backend.storage.Retriever

package object external{
  type Reconciler[R <: Reconcilable] = Retriever[R, Option[ExternalLink[R]]]
  type Links[R <: Reconcilable] = Traversable[ExternalLink[R]]
  type ExtendedLinks[R <: Reconcilable] = Traversable[ExtendedLink[R]]
}
