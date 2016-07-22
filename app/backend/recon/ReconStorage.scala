package backend.recon

import backend.storage.LocalStorageTemplate

import scala.concurrent.ExecutionContext

abstract class ReconStorage[Key <: Reconcilable](implicit ec: ExecutionContext) extends LocalStorageTemplate[Key, (Option[ReconID], Boolean)]
