package songs

import common.json.Jsonable
import common.rich.collections.RichIterable._
import models.Song
import monocle.macros.GenIso

private case class SongGroup(songs: Seq[Song]) {
  require(songs hasAtLeastSizeOf 2, "A SongGroup should have at least two elements")
}

private object SongGroup {
  implicit def songGroupJsonable(implicit ev: Jsonable[Song]): Jsonable[SongGroup] =
    Jsonable.isoJsonable(GenIso.apply[SongGroup, Seq[Song]])
}

