package backend.albums.filler.storage

import backend.albums.filler.NewAlbumRecon
import backend.albums.NewAlbum
import backend.recon.{Artist, ReconID}

import scala.concurrent.Future

import scalaz.ListT

import common.storage.Storage

// TODO Non-Keyed storage, or multi-valued storage
private trait NewAlbumStorage extends Storage[ReconID, StoredNewAlbum] {
  /** Takes care of all the filterings (ignored artists, albums, etc.) */
  def all: ListT[Future, (Artist, Seq[NewAlbum])]
  /**
  * Takes care of all the filterings related to albums(removed, ignored, etc.),
  * but ignored artists will still be returned
  */
  def apply(a: Artist): Future[Seq[NewAlbum]]
  def unremoveAll(a: Artist): Future[Unit]
  def storeNew(albums: Seq[NewAlbumRecon]): Future[Int]

  def remove(artist: Artist): Future[Unit]
  def remove(artist: Artist, albumName: String): Future[Unit]
  def ignore(artist: Artist, albumName: String): Future[Unit]
}
