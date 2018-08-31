package backend.albums

import backend.recon._
import models.IOMusicFinder

private trait NewAlbumsRetrieverFactory {
  def apply(reconciler: ReconcilerCacher[Artist], mf: IOMusicFinder): NewAlbumsRetriever
}


