package backend.external.extensions

import scala.concurrent.Future

import backend.external._
import backend.recon._

private[external] case class ExtendedExternalLinks(
    artistLinks: Future[TimestampedExtendedLinks[Artist]],
    albumLinks: Future[TimestampedExtendedLinks[Album]],
)
