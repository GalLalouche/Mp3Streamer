package backend.recon

import common.storage.LocalStorageTemplate

trait ReconStorage[Key <: Reconcilable] extends LocalStorageTemplate[Key, (Option[ReconID], Boolean)]
