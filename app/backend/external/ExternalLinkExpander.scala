package backend.external

import backend.recon.Reconcilable
import org.jsoup.nodes.Document

abstract class ExternalLinkExpander[T <: Reconcilable](val host: Host) extends (Document => Links[T])
