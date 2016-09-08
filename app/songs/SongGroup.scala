package songs

import models.Song

private case class SongGroup(songs: Set[Song]) {
  require(songs.size > 1)

}

