package songs

import common.json.Jsonable
import common.rich.collections.RichSeq._
import models.Song
import monocle.macros.GenIso
import play.api.libs.json.JsValue

case class SongGroup(songs: Seq[Song]) {
  require(songs hasAtLeastSizeOf 2, "A SongGroup should have at least two elements")
}

object SongGroup {
  implicit def songGroupJsonable(implicit ev: Jsonable[Song]): Jsonable[SongGroup] = Jsonable.isoJsonable(GenIso.apply[SongGroup, Seq[Song]])
}

