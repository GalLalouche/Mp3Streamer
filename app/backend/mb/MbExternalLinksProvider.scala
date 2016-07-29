package backend.mb

import backend.Configuration
import backend.external._
import backend.recon.Reconcilable._
import backend.recon._
import backend.storage.{FreshnessStorage, OnlineRetrieverCacher, RefreshableStorage, Retriever}
import common.RichFuture._
import models.Song
import org.joda.time.Duration

import scala.concurrent.Future

class MbExternalLinksProvider(implicit c: Configuration) extends Retriever[Song, ExternalLinks] {
  import c._
  
  private def createExternalProvider[T <: Reconcilable : Manifest](reconciler: Retriever[T, (Option[ReconID], Boolean)],
                                                        provider: Retriever[ReconID, Links[T]],
                                                        expander: Retriever[Links[T], Links[T]]): Retriever[T, Links[T]] =
    new RefreshableStorage(
      new FreshnessStorage(new SlickExternalStorage[T]()),
      new ExternalPipe[T](
        a => reconciler(a)
            .filterWith(_._1.isDefined, s"Couldn't reconcile <${a}>")
            .map(_._1.get),
        provider,
        expander),
      Duration.standardDays(7))
  private val artistReconciler =
    new ReconcilerCacher[Artist](new ArtistReconStorage, new MbArtistReconciler)
  private val artistPipe = createExternalProvider[Artist](artistReconciler, new ArtistLinkExtractor(), e => Future successful Nil)
  private val albumReconciler =
    new ReconcilerCacher[Album](new AlbumReconStorage, new MbAlbumReconciler(artistReconciler(_).map(_._1.get)))
  private val albumPipe = createExternalProvider(albumReconciler, new AlbumLinkExtractor(), new AlbumLinksExpander())
  
  private def get[T <: Reconcilable](
                                        t: T,
                                        reconciler: Retriever[T, (Option[ReconID], Boolean)],
                                        linkExtractor: Retriever[ReconID, Links[T]]): Future[Traversable[ExternalLink[T]]] =
    reconciler(t)
        .filterWith(_._1.isDefined, s"Couldn't reconcile <$t>")
        .map(_._1.get)
        .flatMap(linkExtractor.apply)
  
  private def getArtist(a: Artist) = artistPipe(a)
  private def getAlbum(a: Album) = albumPipe(a)
  
  override def apply(s: Song): Future[ExternalLinks] =
    getArtist(s.artist)
        .zip(getAlbum(s.release))
        .map(e => ExternalLinks(e._1, e._2, Nil))
}
