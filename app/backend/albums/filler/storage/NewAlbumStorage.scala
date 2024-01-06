package backend.albums.filler.storage

import backend.albums.{AddedAlbumCount, ArtistNewAlbums, NewAlbum}
import backend.albums.filler.NewAlbumRecon
import backend.recon.{Artist, ReconID}
import models.Album.AlbumTitle

import scala.concurrent.Future

import scalaz.ListT

import common.storage.Storage

// TODO Non-Keyed storage, or multi-valued storage
private trait NewAlbumStorage extends Storage[ReconID, StoredNewAlbum] {
  /** Takes care of all the filterings (ignored artists, albums, etc.). */
  def all: ListT[Future, ArtistNewAlbums]
  /**
   * Takes care of all the filterings related to albums(removed, ignored, etc.), but ignored artists
   * will still be returned.
   */
  def apply(a: Artist): Future[Seq[NewAlbum]]
  def unremoveAll(a: Artist): Future[Unit]
  def storeNew(albums: Seq[NewAlbumRecon]): Future[AddedAlbumCount]

  def remove(artist: Artist): Future[Unit]
  def remove(artist: Artist, title: AlbumTitle): Future[Unit]
  def ignore(artist: Artist, title: AlbumTitle): Future[Unit]
}
