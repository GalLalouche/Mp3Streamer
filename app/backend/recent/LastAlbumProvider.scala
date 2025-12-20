package backend.recent

import java.time.LocalDateTime

import models.AlbumDir

trait LastAlbumProvider {
  def since(since: LocalDateTime): Seq[AlbumDir]
}
