package backend.external.expansions

import backend.configs.Configuration
import backend.recon.{Album, Artist}

private[external] object LinkExpanders {
  def artists(implicit c: Configuration): Traversable[ExternalLinkExpander[Artist]] =
    List(new WikidataEnglishExtender[Artist])
  def albums(implicit c: Configuration): Traversable[ExternalLinkExpander[Album]] =
    List(new WikipediaAlbumExternalLinksExpander, new WikidataEnglishExtender)
}
