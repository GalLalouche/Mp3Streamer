package backend.external.expansions

import backend.logging.LoggerProvider
import backend.recon.Album
import common.io.InternetTalker

private[external] object LinkExpanders {
  def albums(implicit it: InternetTalker, lp: LoggerProvider): Traversable[ExternalLinkExpander[Album]] =
    List(new WikipediaAlbumExternalLinksExpander)
}
