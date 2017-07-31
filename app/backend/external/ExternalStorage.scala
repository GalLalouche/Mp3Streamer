package backend.external

import java.time.LocalDateTime

import backend.recon.Reconcilable
import backend.storage.StorageTemplate

import scala.concurrent.ExecutionContext

private[external] abstract class ExternalStorage[Key <: Reconcilable](implicit ec: ExecutionContext)
    extends StorageTemplate[Key, (MarkedLinks[Key], Option[LocalDateTime])]
