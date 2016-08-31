package backend.external

import backend.recon.Reconcilable
import backend.storage.LocalStorageTemplate
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext

private[external] abstract class ExternalStorage[Key <: Reconcilable](implicit ec: ExecutionContext)
    extends LocalStorageTemplate[Key, (Links[Key], Option[DateTime])]
