package songs

import models.Song

case class SongGroup(songs: Set[Song]) {
  require(songs.size > 1)
}

