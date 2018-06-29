package backend.external.expansions

import backend.logging.LoggerProvider
import backend.recon.{Album, Artist}
import common.io.InternetTalker

private[external] object LinkExpanders {
  def artists(implicit it: InternetTalker): Traversable[ExternalLinkExpander[Artist]] =
    List(new WikidataEnglishExtender[Artist])
  def albums(implicit it: InternetTalker, lp: LoggerProvider): Traversable[ExternalLinkExpander[Album]] =
    List(new WikipediaAlbumExternalLinksExpander, new WikidataEnglishExtender)
}
