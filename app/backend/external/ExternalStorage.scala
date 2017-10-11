package backend.external

import java.time.LocalDateTime

import backend.recon.Reconcilable
import backend.storage.StorageTemplate

private[external] trait ExternalStorage[R <: Reconcilable] extends
    StorageTemplate[R, (MarkedLinks[R], Option[LocalDateTime])]
