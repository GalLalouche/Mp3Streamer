package backend.albums.filler.storage

import backend.albums.{ArtistNewAlbums, NewAlbum}
import backend.recon.{Artist, IgnoredReconResult}
import models.Album.AlbumTitle

import scala.concurrent.Future

import scalaz.ListT

private[albums] trait FilledStorage {
  def all: ListT[Future, ArtistNewAlbums]
  def forArtist(a: Artist): Future[Seq[NewAlbum]]
  def newArtist(artist: Artist): Future[Unit]

  // Remove vs. Ignore: remove is only temporary (usually until the next mega fetch), ignore is forever.
  // Reasons to remove: you're not going do anything with the artist/album this time.
  // Reasons to ignore artist: you don't care about completing the artist's discography.
  // Reasons to ignore album: you don't care about the album, or it's an unreconned version of something
  //                          you already have.

  def remove(artist: Artist): Future[Unit]
  def ignore(artist: Artist): Future[Unit]
  def isIgnored(artist: Artist): Future[IgnoredReconResult]
  def unignore(artist: Artist): Future[Unit]
  def remove(artist: Artist, title: AlbumTitle): Future[Unit]
  def ignore(artist: Artist, title: AlbumTitle): Future[Unit]
}
