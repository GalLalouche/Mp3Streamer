package backend.lyrics.retrievers

import models.TypeAliases.ArtistName

import scala.concurrent.Future

import common.storage.Storage

private[lyrics] trait InstrumentalArtistStorage extends Storage[ArtistName, Unit] {
  def store(name: ArtistName): Future[Unit]
}
