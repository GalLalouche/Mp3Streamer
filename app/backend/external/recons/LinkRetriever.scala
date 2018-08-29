package backend.external.recons

import backend.Retriever
import backend.external.{BaseLink, Host}
import backend.recon.Reconcilable

private[external] abstract class LinkRetriever[R <: Reconcilable](val host: Host) extends Retriever[R, Option[BaseLink[R]]]