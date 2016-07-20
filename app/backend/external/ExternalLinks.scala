package backend.external

case class ExternalLinks(artistLinks: Traversable[ExternalLink],
                         albumLinks: Traversable[ExternalLink],
                         trackLinks: Traversable[ExternalLink])
