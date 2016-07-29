package backend

import backend.recon.Reconcilable

package object external{
  type Links[T <: Reconcilable] = Traversable[ExternalLink[T]]
}
