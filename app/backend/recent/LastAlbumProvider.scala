package backend.recent

import java.time.Instant

import models.AlbumDir

trait LastAlbumProvider {
  def since(since: Instant): Seq[AlbumDir]
}
