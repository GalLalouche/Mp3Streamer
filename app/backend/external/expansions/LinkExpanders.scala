package backend.external.expansions

import backend.recon.Album
import common.io.InternetTalker

private[external] object LinkExpanders {
  def albums(implicit it: InternetTalker): Traversable[ExternalLinkExpander[Album]] =
    List(new WikipediaAlbumExternalLinksExpander)
}
