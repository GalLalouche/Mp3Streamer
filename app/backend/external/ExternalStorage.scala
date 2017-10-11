package backend.external

import java.time.LocalDateTime

import backend.recon.Reconcilable
import common.storage.Storage

private[external] trait ExternalStorage[R <: Reconcilable] extends Storage[R, (MarkedLinks[R], Option[LocalDateTime])]
