package backend.id3

import models.Song

trait Id3Updater {
  def update(song: Song, newData: Id3Metadata): Unit
}
