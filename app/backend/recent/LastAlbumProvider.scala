package backend.recent

import models.AlbumDir

import scala.concurrent.Future

trait LastAlbumProvider {
  def last: Future[AlbumDir]
}
