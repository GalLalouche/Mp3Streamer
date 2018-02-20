package playlist

import common.json.ToJsonableOps
import common.rich.RichT._
import models.Song
import play.api.libs.json.{Format, JsValue}

private case class PlaylistQueue(songs: Seq[Song])

private object PlaylistQueue extends ToJsonableOps {
  import common.json.Jsonable

  implicit def PlaylistJsonable(implicit songJsonable: Format[Song]): Jsonable[PlaylistQueue] =
    new Jsonable[PlaylistQueue] {
      override def jsonify(p: PlaylistQueue): JsValue = p.songs.jsonify
      override def parse(json: JsValue): PlaylistQueue = json.parse[Seq[Song]] |> PlaylistQueue.apply
    }
}
