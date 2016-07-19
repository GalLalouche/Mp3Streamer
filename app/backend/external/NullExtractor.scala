package backend.external

import backend.recon.ReconID

import scala.concurrent.Future

object NullExtractor extends ExternalLinkProvider {
  override def apply(id: ReconID): Future[Traversable[ExternalLink]] = Future successful Nil
}
