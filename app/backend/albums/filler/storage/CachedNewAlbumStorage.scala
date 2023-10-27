package backend.albums.filler.storage

import backend.albums.filler.NewAlbumRecon
import backend.albums.AddedAlbumCount
import backend.recon.Artist
import backend.storage.Freshness

import scala.concurrent.Future

import scalaz.OptionT

private[filler] trait CachedNewAlbumStorage extends FilledStorage {
  def reset(a: Artist): Future[Freshness]
  def freshness(a: Artist): OptionT[Future, Freshness]
  def unremoveAll(a: Artist): Future[Unit]
  /**
   * Will not touch existing albums since those might have their isRemoved/isIgnored set.
   * Will also update the provided artists last fetch time.
   *
   * @param artists although this could theoretically be extracted from albums, it's possible that for
   *                a given artist no new albums will be found, but we still want to update the artist
   *                last fetch time for those cases.
   */
  def storeNew(albums: Seq[NewAlbumRecon], artists: Set[Artist]): Future[AddedAlbumCount]
}
