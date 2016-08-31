package backend.external.recons

import backend.Retriever
import backend.external.{ExternalLink, Host}
import backend.recon.Reconcilable

abstract class Reconciler[R <: Reconcilable](val host: Host) extends Retriever[R, Option[ExternalLink[R]]]
