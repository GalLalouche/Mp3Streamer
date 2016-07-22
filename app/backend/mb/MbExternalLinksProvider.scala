package backend.mb

import backend.external.{ExternalLink, ExternalLinkProvider, ExternalLinks, ExternalLinksProvider}
import backend.recon.Reconcilable._
import backend.recon._
import common.RichFuture._
import models.Song

import scala.concurrent.{ExecutionContext, Future}

class MbExternalLinksProvider(implicit ec: ExecutionContext) extends ExternalLinksProvider {
  private val artistLinkExtractor = new ArtistLinkExtractor
  private val artistReconciler = new MbArtistReconcilerCacher
  private val albumLinkExtractor = new AlbumLinkExtractor
  private val albumReconciler =
    new ReconcilerCacher[Album](new AlbumReconStorage(), new MbAlbumReconciler(artistReconciler(_).map(_._1.get)))

  private def get[T](t: T,
                     reconciler: T => Future[(Option[ReconID], Boolean)],
                     linkExtractor: ExternalLinkProvider): Future[Traversable[ExternalLink]] =
    reconciler(t)
      .filterWith(_._1.isDefined, s"Couldn't reconcile <$t>")
      .map(_._1.get)
      .flatMap(linkExtractor.apply)

  private def getArtist(a: Artist) = get(a, artistReconciler, artistLinkExtractor)
  private def getAlbum(a: Album) = get(a, albumReconciler, albumLinkExtractor)

  override def getExternalLinks(s: Song): Future[ExternalLinks] =
    getArtist(s.artist)
      .zip(getAlbum(s.release))
      .map(e => ExternalLinks(e._1, e._2, Nil))
}
