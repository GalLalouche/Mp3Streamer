package backend.external

import backend.external.expansions.CompositeSameHostExpander
import backend.external.extensions._
import backend.external.recons.LinkRetrievers
import backend.new_albums.filler.storage.NewAlbumCleaner
import backend.recon._
import backend.recon.Reconcilable._
import com.google.inject.Inject
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import cats.implicits.catsSyntaxFlatMapOps

private class MbExternalLinksProvider @Inject() (
    ec: ExecutionContext,
    artistReconStorage: ArtistReconStorage,
    artistExternalStorage: ArtistExternalStorage,
    artistLinkRetrievers: LinkRetrievers[Artist],
    albumLinkRetrievers: LinkRetrievers[Album],
    compositeSameHostExpander: CompositeSameHostExpander,
    albumReconStorage: AlbumReconStorage,
    albumExternalStorage: AlbumExternalStorage,
    newAlbumStorage: NewAlbumCleaner,
    extender: CompositeExtender,
    artistPipeWrapper: ExternalPipeWrapper[Artist],
    albumPipeWrapper: ExternalPipeWrapper[Album],
) {
  private implicit val iec: ExecutionContext = ec
  private val artistPipe = artistPipeWrapper(artistLinkRetrievers)

  private def getAlbumLinks(
      artistLinks: MarkedLinks[Artist],
      album: Album,
  ): Future[TimestampedLinks[Album]] =
    albumPipeWrapper(
      compositeSameHostExpander.toReconcilers(artistLinks.map(_.toBase)) ++ albumLinkRetrievers,
    ).apply(album)

  // for testing on remote
  private[external] def apply(a: Album): ExtendedExternalLinks = {
    val artistLinks: Future[TimestampedLinks[Artist]] = artistPipe(a.artist)
    val albumLinks: Future[TimestampedLinks[Album]] =
      artistLinks.map(_.links).flatMap(getAlbumLinks(_, a))
    ExtendedExternalLinks(artistLinks.map(extender(a.artist, _)), albumLinks.map(extender(a, _)))
  }
  def apply(s: Song): ExtendedExternalLinks = apply(s.release)

  def deleteArtist(song: Song): Future[_] = artistExternalStorage.delete(song.artist).value
  def deleteAlbum(song: Song): Future[_] = albumExternalStorage.delete(song.release).value

  def updateRecon(song: Song): UpdatedRecon => Future[_] = {
    case UpdatedRecon.Artist(reconId) =>
      // If artist recon was updated, all existing release updates should be deleted
      val artist = song.artist
      artistExternalStorage.delete(artist).value >>
        albumExternalStorage.deleteAllLinks(artist) >>
        newAlbumStorage.deleteAll(artist) >>
        artistReconStorage.update(artist, reconId).value
    case UpdatedRecon.Album(reconId) =>
      val release = song.release
      albumReconStorage.delete(release).value >>
        albumExternalStorage.delete(release).value >>
        albumReconStorage.update(release, reconId).value
  }
}
