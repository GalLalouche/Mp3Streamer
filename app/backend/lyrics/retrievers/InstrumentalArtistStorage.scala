package backend.lyrics.retrievers

import scala.concurrent.Future

import common.storage.Storage

private[lyrics] trait InstrumentalArtistStorage extends Storage[String, Unit] {
  def store(artistName: String): Future[Unit]
}
