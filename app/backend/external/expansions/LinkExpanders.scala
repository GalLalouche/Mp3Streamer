package backend.external.expansions

import backend.recon.Album
import common.io.InternetTalker

import scala.concurrent.ExecutionContext

private[external] object LinkExpanders {
  def albums(implicit ec: ExecutionContext, it: InternetTalker): Traversable[ExternalLinkExpander[Album]] =
    List(new WikipediaAlbumExternalLinksExpander)
}
