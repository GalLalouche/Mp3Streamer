package backend.new_albums.filler.storage

import backend.recon.Artist

import scala.concurrent.Future

trait NewAlbumCleaner {
  /**
   * Deletes all new albums for an artist. This isn't the same as remove, since that only marks the
   * albums as removed. This should be used when the artist's recon is updated, since that means the
   * albums don't belong to this artist.
   */
  def deleteAll(a: Artist): Future[Unit]
}
