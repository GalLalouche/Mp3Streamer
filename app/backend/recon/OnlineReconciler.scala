package backend.recon

import backend.storage.Retriever

import scala.concurrent.Future

trait OnlineReconciler[Key <: Reconcilable] extends Retriever[Key, Option[ReconID]]
