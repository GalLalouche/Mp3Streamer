package backend.recon

import models.TypeAliases.ArtistName

import scala.concurrent.Future

trait ArtistReconPusher {
  def withValidation(artistName: ArtistName, reconId: String, isIgnored: Boolean): Future[Unit]
}
