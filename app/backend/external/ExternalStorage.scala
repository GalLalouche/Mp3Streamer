package backend.external

import backend.recon.Reconcilable
import backend.storage.StorageTemplate
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext

private[external] abstract class ExternalStorage[Key <: Reconcilable](implicit ec: ExecutionContext)
    extends StorageTemplate[Key, (BaseLinks[Key], Option[DateTime])]
