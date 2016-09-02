package backend.external.extensions

import backend.external._
import backend.recon._

case class ExtendedExternalLinks(artistLinks: TimestampedExtendedLinks[Artist], albumLinks: TimestampedExtendedLinks[Album])
