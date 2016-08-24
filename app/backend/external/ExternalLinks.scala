package backend.external

import backend.recon._

case class ExternalLinks(artistLinks: Links[Artist],
                         albumLinks: Links[Album],
                         trackLinks: Links[Track])
