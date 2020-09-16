package backend.albums.filler.storage

import backend.albums.NewAlbum
import backend.recon.{Artist, ReconID}

import scala.concurrent.Future

private[albums] trait FilledStorage {
  type AlbumReconID = ReconID
  type ArtistReconID = ReconID
  def all: Future[Map[Artist, Seq[NewAlbum]]]

  // Remove vs. Ignore: remove is only temporary (usually until the next mega fetch), ignore is forever.
  // Reasons to remove: you're not going do anything with the artist/album this time.
  // Reasons to ignore artist: you don't care about completing the artist's discography.
  // Reasons to ignore album: you don't care about the album, or it's an unreconned version of something
  //                          you already have.

  def remove(artist: Artist): Future[Unit]
  def ignore(artist: Artist): Future[Unit]
  def remove(artist: Artist, albumName: String): Future[Unit]
  def ignore(artist: Artist, albumName: String): Future[Unit]
}
