package backend.albums

import backend.recon._

private trait NewAlbumsRetrieverFactory {
  def apply(reconciler: ReconcilerCacher[Artist]): NewAlbumsRetriever
}


