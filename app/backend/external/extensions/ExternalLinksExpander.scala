package backend.external.extensions

import backend.external.{ExternalLink, _}
import backend.recon.{Album, Reconcilable}
import backend.Retriever
import common.io.InternetTalker

import scala.concurrent.{ExecutionContext, Future}

object LinkExpanders {
  def albums(implicit ec: ExecutionContext, it: InternetTalker): Traversable[ExternalLinkExpander[Album]] = List(new WikipediaAlbumExternalLinksExpander)
}
