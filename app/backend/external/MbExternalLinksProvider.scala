package backend.external

import backend.external.expansions.CompositeSameHostExpander
import backend.external.extensions._
import backend.external.recons.LinkRetrievers
import backend.recon._
import backend.recon.Reconcilable._
import common.rich.func.{ToMoreFoldableOps, ToMoreMonadErrorOps}
import common.rich.primitives.RichOption._
import javax.inject.Inject
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.{FutureInstances, OptionInstances}
import scalaz.syntax.{ToBindOps, ToFunctorOps}

// TODO split to artist and album
private class MbExternalLinksProvider @Inject()(
    ec: ExecutionContext,
    artistReconStorage: ArtistReconStorage,
    artistExternalStorage: ArtistExternalStorage,
    artistLinkRetrievers: LinkRetrievers[Artist],
    albumLinkRetrievers: LinkRetrievers[Album],
    compositeSameHostExpander: CompositeSameHostExpander,
    albumReconStorage: AlbumReconStorage,
    albumExternalStorage: AlbumExternalStorage,
    extender: CompositeExtender,
    mbAlbumReconciler: Reconciler[Album],
    artistReconciler: ReconcilerCacher[Artist],
    artistPipeWrapper: ExternalPipeWrapper[Artist],
    albumPipeWrapper: ExternalPipeWrapper[Album],
) extends ToMoreFoldableOps with ToFunctorOps with ToBindOps with ToMoreMonadErrorOps
    with FutureInstances with OptionInstances {
  private implicit val iec: ExecutionContext = ec
  private val artistPipe = artistPipeWrapper(artistReconciler, artistLinkRetrievers)

  private def getAlbumLinks(artistLinks: MarkedLinks[Artist], album: Album): Future[TimestampedLinks[Album]] =
    albumPipeWrapper(
      new ReconcilerCacher[Album](albumReconStorage, mbAlbumReconciler),
      compositeSameHostExpander.toReconcilers(artistLinks.map(_.toBase)) ++ albumLinkRetrievers,
    ) apply album

  // for testing on remote
  private def apply(a: Album): ExtendedExternalLinks = {
    val artistLinks: Future[TimestampedLinks[Artist]] = artistPipe(a.artist)
    val albumLinks: Future[TimestampedLinks[Album]] = artistLinks.map(_.links).flatMap(getAlbumLinks(_, a))
    ExtendedExternalLinks(artistLinks.map(extender.apply(a.artist, _)), albumLinks.map(extender.apply(a, _)))
  }
  def apply(s: Song): ExtendedExternalLinks = apply(s.release)

  private def update[R <: Reconcilable](key: R, recon: Option[ReconID], storage: ReconStorage[R]): Future[_] =
    recon.transformer[Future].flatMapF(storage.update(key, _)).run

  def delete(song: Song): Future[_] =
    artistExternalStorage.delete(song.artist) >> albumExternalStorage.delete(song.release)
  def updateRecon(song: Song, artistReconId: Option[ReconID], albumReconId: Option[ReconID]): Future[_] = {
    require(artistReconId.isDefined || albumReconId.isDefined, "No actual recon IDs given")
    update(song.artist, artistReconId, artistReconStorage).>>(
      if (artistReconId.isDefined) {
        artistExternalStorage.delete(song.artist) >>
            albumReconStorage.deleteAllRecons(song.artist) >>
            albumExternalStorage.deleteAllLinks(song.artist)
      } else {
        assert(albumReconId.isDefined)
        albumReconStorage.delete(song.release) >>
            albumExternalStorage.delete(song.release)
      }).>>(update(song.release, albumReconId, albumReconStorage))
  }
}
