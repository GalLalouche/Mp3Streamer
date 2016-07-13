package backend.external

import scala.concurrent.Future

trait ExternalLinksProvider {
  def getExternalLinks(artistName: String): Future[Traversable[ExternalLink]]
}
