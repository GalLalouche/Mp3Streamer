package backend.albums.filler

import scala.concurrent.Future

import com.google.inject.ImplementedBy

@ImplementedBy(classOf[ArtistReconPusherImpl])
trait ArtistReconPusher {
  def withValidation(artistName: String, reconId: String, isIgnored: Boolean): Future[Unit]
}
