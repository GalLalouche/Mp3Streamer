package songs

import models.Song

case class SongGroup(songs: Seq[Song]) {
  require(songs.size > 1)
}

