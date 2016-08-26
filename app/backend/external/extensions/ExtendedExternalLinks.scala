package backend.external.extensions

import backend.external._
import backend.recon._

case class ExtendedExternalLinks(artistLinks: ExtendedLinks[Artist],
                                 albumLinks: ExtendedLinks[Album],
                                 trackLinks: ExtendedLinks[Track])
