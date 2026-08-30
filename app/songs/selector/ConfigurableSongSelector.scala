package songs.selector

import models.Song

import common.Filter
import common.path.ref.FileRef

// TODO could also be an interesting SD question
trait ConfigurableSongSelector extends SongSelector {
  def randomSong(): Song
  def randomMp3Song(): Song = randomSongWithExtension("mp3")
  def randomFlacSong(): Song = randomSongWithExtension("flac")
  def withAdditionalFileFilter(filter: Filter[FileRef]): ConfigurableSongSelector

  private def randomSongWithExtension(ext: String): Song =
    withAdditionalFileFilter(_.hasExtension(ext)).randomSong()
}
