package backend.external.expansions

import backend.recon.{Album, Artist}
import javax.inject.Inject

private[external] class ArtistLinkExpanders @Inject()(f: WikidataEnglishExtenderFactory) {
  def get: Traversable[ExternalLinkExpander[Artist]] =
    Vector(f.create)
}
private[external] class AlbumLinkExpanders @Inject()(
    f: WikidataEnglishExtenderFactory,
    w: WikipediaAlbumExternalLinksExpander,
) {
  def get: Traversable[ExternalLinkExpander[Album]] =
    Vector(f.create, w)
}
