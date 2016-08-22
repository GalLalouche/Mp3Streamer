package backend.mb

import backend.Configuration
import backend.external._
import backend.recon.Reconcilable._
import backend.recon._
import backend.storage.{FreshnessStorage, RefreshableStorage, Retriever}
import common.rich.RichFuture._
import common.rich.RichT._
import models.Song
import org.joda.time.Duration

import scala.concurrent.Future
import scalaz._
import Scalaz._

class MbExternalLinksProvider(implicit c: Configuration) extends Retriever[Song, ExternalLinks] {
  private def createExternalProvider[T <: Reconcilable : Manifest](
      reconciler: Retriever[T, (Option[ReconID], Boolean)],
      provider: Retriever[ReconID, Links[T]],
      expander: Retriever[Links[T], Links[T]]): Retriever[T, Links[T]] =
    new RefreshableStorage(
      new FreshnessStorage(new SlickExternalStorage[T]),
      new ExternalPipe[T](
        a => reconciler(a)
            .filterWith(_._1.isDefined, s"Couldn't reconcile <$a>")
            .map(_._1.get),
        provider,
        expander),
      Duration.standardDays(7))

  private val artistReconciler =
    new ReconcilerCacher[Artist](new ArtistReconStorage, new MbArtistReconciler)
  private val albumReconciler =
    new ReconcilerCacher[Album](new AlbumReconStorage, new MbAlbumReconciler(artistReconciler(_).map(_._1.get)))

  private val artistPipe =
    createExternalProvider[Artist](artistReconciler, new ArtistLinkExtractor, Future.successful(Nil).const)

  private val extractor = new AlbumLinkExtractor
  private val sameHostExpander = CompositeSameHostExpander.default
  private def superExtractor(artistLinks: Links[Artist], a: Album): Retriever[Links[Album], Links[Album]] = links => {
    new AlbumLinksExpander().apply(links).zip(sameHostExpander.apply(artistLinks, a)).map(_.toList.flatten)
  }

  private def getArtistLinks(a: Artist) = artistPipe(a)
  private def getAlbumLinks(artistLinks: Links[Artist], a: Album) =
    createExternalProvider(albumReconciler,
      extractor,
      superExtractor(artistLinks, a)).apply(a)

  override def apply(s: Song): Future[ExternalLinks] =
    for (artistLinks <- getArtistLinks(s.artist); albumLinks <- getAlbumLinks(artistLinks, s.release))
      yield ExternalLinks(artistLinks, albumLinks, Nil)
}
