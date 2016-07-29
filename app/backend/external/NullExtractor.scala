package backend.external

import backend.recon.ReconID

import scala.concurrent.Future

object NullExtractor extends ExternalLinkProvider[Nothing] {
  override def apply(id: ReconID): Future[Links[Nothing]] = Future successful Nil
}
