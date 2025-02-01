package backend.new_albums.filler.storage

import backend.recon.Artist
import backend.storage.Freshness

import scala.concurrent.Future

import scalaz.OptionT

// TODO UnitRefreshableStorage
private trait LastFetchTime {
  def update(a: Artist): Future[Unit]
  def ignore(a: Artist): Future[Unit]
  def unignore(a: Artist): Future[Freshness]
  /** Returns [[None]] if the artist is not reconciled. Should always return a value otherwise. */
  def freshness(a: Artist): OptionT[Future, Freshness]
  /** Returns epoch freshness for convenience. Will fail if the artist is not reconciled. */
  def resetToEpoch(a: Artist): Future[Freshness]
}
