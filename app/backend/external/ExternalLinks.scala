package backend.external

import backend.recon._

case class ExternalLinks(artistLinks: Traversable[ExternalLink[Artist]],
                         albumLinks: Traversable[ExternalLink[Album]],
                         trackLinks: Traversable[ExternalLink[Track]])
