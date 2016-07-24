package backend.mb

import backend.Configuration
import backend.external._
import backend.recon.Reconcilable._
import backend.recon._
import backend.storage.Retriever
import common.RichFuture._
import models.Song

import scala.concurrent.Future

class MbExternalLinksProvider(implicit c: Configuration) extends ExternalLinksProvider {
  import c._
  private val artistLinkExtractor = new ArtistLinkExtractor
  private val artistReconciler =
    new ReconcilerCacher[Artist](new ArtistReconStorage, new MbArtistReconciler)
  private val albumLinkExtractor = new AlbumLinkExtractor
  private val albumReconciler =
    new ReconcilerCacher[Album](new AlbumReconStorage, new MbAlbumReconciler(artistReconciler(_).map(_._1.get)))

  private def get[T <: Reconcilable](t: T,
                     reconciler: Retriever[T, (Option[ReconID], Boolean)],
                     linkExtractor: ExternalLinkProvider[T]): Future[Traversable[ExternalLink[T]]] =
    reconciler(t)
      .filterWith(_._1.isDefined, s"Couldn't reconcile <$t>")
      .map(_._1.get)
      .flatMap(linkExtractor.apply)

  private def getArtist(a: Artist) = get(a, artistReconciler, artistLinkExtractor)
  private def getAlbum(a: Album) = get(a, albumReconciler, albumLinkExtractor)

  override def apply(s: Song): Future[ExternalLinks] =
    getArtist(s.artist)
      .zip(getAlbum(s.release))
      .map(e => ExternalLinks(e._1, e._2, Nil))
}
