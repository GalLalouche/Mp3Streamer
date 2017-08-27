package backend.external.extensions

import backend.external._
import backend.recon._

import scala.concurrent.Future

private[external] case class ExtendedExternalLinks(
    artistLinks: Future[TimestampedExtendedLinks[Artist]],
    albumLinks: Future[TimestampedExtendedLinks[Album]])
