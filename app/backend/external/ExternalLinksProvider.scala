package backend.external

import models.Song

import scala.concurrent.Future

trait ExternalLinksProvider {
  def getExternalLinks(s: Song): Future[ExternalLinks]
}
