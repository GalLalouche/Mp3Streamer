package backend.albums.filler.storage

import backend.recon.Artist
import backend.storage.Freshness

import scala.concurrent.Future

import scalaz.OptionT

// TODO UnitRefreshableStorage
private trait LastFetchTime {
  def update(a: Artist): Future[Unit]
  def ignore(a: Artist): Future[Unit]
  def freshness(a: Artist): OptionT[Future, Freshness]
  def reset(a: Artist): Future[Freshness]
}
