package backend.lyrics.retrievers

import backend.recon.Artist

import scala.concurrent.Future

import common.storage.Storage

private[lyrics] trait InstrumentalArtistStorage extends Storage[Artist, Unit] {
  def store(name: Artist): Future[Unit]
}
