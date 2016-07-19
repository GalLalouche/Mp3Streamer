package backend.external

import backend.recon.ReconID

import scala.concurrent.Future

trait ExternalLinkProvider {
  def apply(id: ReconID): Future[Traversable[ExternalLink]]
}
