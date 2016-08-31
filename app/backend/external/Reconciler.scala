package backend.external

import backend.Retriever
import backend.recon.Reconcilable

abstract class Reconciler[R <: Reconcilable](val host: Host) extends Retriever[R, Option[ExternalLink[R]]]
