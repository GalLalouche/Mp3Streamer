package songs

import common.rich.collections.RichSeq._
import models.Song

case class SongGroup(songs: Seq[Song]) {
  require(songs hasAtLeastSizeOf 2, "A SongGroup should have at least two elements")
}

