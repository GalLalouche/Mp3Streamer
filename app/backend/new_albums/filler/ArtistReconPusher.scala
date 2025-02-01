package backend.new_albums.filler

import com.google.inject.ImplementedBy
import models.TypeAliases.ArtistName

import scala.concurrent.Future

@ImplementedBy(classOf[ArtistReconPusherImpl])
trait ArtistReconPusher {
  def withValidation(artistName: ArtistName, reconId: String, isIgnored: Boolean): Future[Unit]
}
