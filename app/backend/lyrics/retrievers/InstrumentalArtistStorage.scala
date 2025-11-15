package backend.lyrics.retrievers

import backend.recon.Artist
import com.google.common.annotations.VisibleForTesting

import scala.concurrent.Future

import common.storage.Storage

@VisibleForTesting private[lyrics] trait InstrumentalArtistStorage extends Storage[Artist, Unit] {
  def store(name: Artist): Future[Unit]
}
