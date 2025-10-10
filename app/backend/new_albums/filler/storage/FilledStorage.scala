package backend.new_albums.filler.storage

import backend.new_albums.{ArtistNewAlbums, NewAlbum}
import backend.recon.{Artist, IgnoredReconResult, ReconID}

import scala.concurrent.Future

import common.TempIList.ListT

private[new_albums] trait FilledStorage {
  def all: ListT[Future, ArtistNewAlbums]
  def forArtist(a: Artist): Future[Seq[NewAlbum]]

  // Remove vs. Ignore: remove is only temporary (usually until the next mega fetch), ignore is forever.
  // Reasons to remove: you're not going do anything with the artist/album this time.
  // Reasons to ignore artist: you don't care about completing the artist's discography.
  // Reasons to ignore album: you don't care about the album, or it's an unreconned version of something
  //                          you already have.

  def remove(artist: Artist): Future[Unit]
  def ignore(artist: Artist): Future[Unit]
  def isIgnored(artist: Artist): Future[IgnoredReconResult]
  def unignore(artist: Artist): Future[Unit]
  def removeAlbum(reconID: ReconID): Future[Unit]
  def ignore(reconID: ReconID): Future[Unit]
}
