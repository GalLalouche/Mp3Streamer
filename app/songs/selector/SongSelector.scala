package songs.selector

import models.Song

trait SongSelector {
  def randomSong(): Song
  def randomMp3Song(): Song
  def randomFlacSong(): Song
}
