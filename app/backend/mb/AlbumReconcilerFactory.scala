package backend.mb

import backend.Retriever
import backend.recon._

trait AlbumReconcilerFactory {
  def apply(artistReconciler: Retriever[Artist, ReconID]): Reconciler[Album]
}
