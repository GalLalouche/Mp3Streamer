package backend.mb

import backend.external.{ExternalLinks, ExternalLinksProvider, NullExtractor}
import models.Song
import common.RichFuture._

import scala.concurrent.{ExecutionContext, Future}
import backend.recon.Reconcilable._

class MbExternalLinksProvider(implicit ec: ExecutionContext) extends ExternalLinksProvider {
  private val artistExtractor = ArtistLinkExtractor
  private val artistReconciler = MbArtistReconcilerCacher

  override def getExternalLinks(s: Song): Future[ExternalLinks] =
    artistReconciler.get(s.artist)
      .filterWith(_._1.isDefined, s"Couldn't reconcile <${s.artist}>")
      .map(_._1.get)
      .flatMap(artistExtractor.apply)
      .map(ExternalLinks(_, Nil, Nil))
}
