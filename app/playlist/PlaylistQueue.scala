package playlist

import common.Jsonable
import models.Song
import play.api.libs.json.JsValue

private case class PlaylistQueue(songs: Seq[Song])

private object PlaylistQueue extends Jsonable.ToJsonableOps {
  import common.Jsonable
  import common.rich.RichT._

  implicit def PlaylistJsonable(implicit songJsonable: Jsonable[Song]): Jsonable[PlaylistQueue] =
    new Jsonable[PlaylistQueue] {
      override def jsonify(p: PlaylistQueue): JsValue = p.songs.jsonify
      override def parse(json: JsValue): PlaylistQueue = json.parse[Seq[Song]] |> PlaylistQueue.apply
    }
}
