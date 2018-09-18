package backend.external

import backend.recon.Reconcilable
import backend.storage.Freshness
import common.storage.Storage

private[external] trait ExternalStorage[R <: Reconcilable] extends Storage[R, (MarkedLinks[R], Freshness)]
