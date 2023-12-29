package backend.albums.filler

import com.google.inject.ImplementedBy

import scala.concurrent.Future

@ImplementedBy(classOf[ArtistReconPusherImpl])
trait ArtistReconPusher {
  def withValidation(artistName: String, reconId: String, isIgnored: Boolean): Future[Unit]
}
