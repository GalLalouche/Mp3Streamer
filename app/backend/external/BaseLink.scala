package backend.external

import backend.Url
import backend.recon.Reconcilable

private[external] case class BaseLink[T <: Reconcilable](link: Url, host: Host)
