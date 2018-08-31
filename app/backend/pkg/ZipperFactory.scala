package backend.pkg

import models.Song

private trait ZipperFactory {
  def apply(songRemotePathEncoder: Song => String): Zipper
}
