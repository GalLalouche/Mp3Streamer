package backend.recon

import backend.storage.StorageTemplate

import scala.concurrent.ExecutionContext

abstract class ReconStorage[Key <: Reconcilable](implicit ec: ExecutionContext) extends StorageTemplate[Key, (Option[ReconID], Boolean)]
