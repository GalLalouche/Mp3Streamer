package backend.new_albums.filler.storage

import backend.new_albums.{AddedAlbumCount, ArtistNewAlbums, NewAlbum}
import backend.new_albums.filler.NewAlbumRecon
import backend.recon.{Artist, ReconID}

import scala.concurrent.Future

import scalaz.ListT

import common.storage.Storage

// TODO Non-Keyed storage, or multi-valued storage
private trait NewAlbumStorage extends Storage[ReconID, StoredNewAlbum] with NewAlbumCleaner {
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
  def removeAlbum(reconID: ReconID): Future[Unit]
  def ignoreAlbum(reconID: ReconID): Future[Unit]
}
