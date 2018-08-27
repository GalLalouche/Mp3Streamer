package backend.external.expansions

import backend.configs.Configuration
import backend.recon.{Album, Artist}
import net.codingwell.scalaguice.InjectorExtensions._

private[external] object LinkExpanders {
  def artists(implicit c: Configuration): Traversable[ExternalLinkExpander[Artist]] =
    List(new WikidataEnglishExtender[Artist])
  def albums(implicit c: Configuration): Traversable[ExternalLinkExpander[Album]] =
    List(c.injector.instance[WikipediaAlbumExternalLinksExpander], new WikidataEnglishExtender)
}
