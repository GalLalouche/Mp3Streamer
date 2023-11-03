package songs

import monocle.macros.GenIso

import common.json.Jsonable
import common.rich.collections.RichIterable._
import models.Song

private case class SongGroup(songs: Seq[Song]) {
  require(songs.hasAtLeastSizeOf(2), "A SongGroup should have at least two elements")
}

private object SongGroup {
  implicit def songGroupJsonable(implicit ev: Jsonable[Song]): Jsonable[SongGroup] =
    Jsonable.isoJsonable(GenIso.apply[SongGroup, Seq[Song]])
}
