package backend.external

import backend.Url
import backend.recon.Reconcilable
import org.jsoup.nodes.Document

abstract class ExternalLinkExpender[T <: Reconcilable](val host: Host) extends (Document => Traversable[ExternalLink[T]])
