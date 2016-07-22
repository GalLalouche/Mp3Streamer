package backend.recon

import backend.storage.LocalStorageTemplate

trait ReconStorage[Key <: Reconcilable] extends LocalStorageTemplate[Key, (Option[ReconID], Boolean)]
