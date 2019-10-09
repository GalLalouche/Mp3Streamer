package playlist

import models.Song
import play.api.libs.json.JsValue

import common.json.ToJsonableOps._
import common.rich.RichT._

private case class PlaylistQueue(songs: Seq[Song])

private object PlaylistQueue {
  import common.json.Jsonable

  implicit def PlaylistJsonable(implicit songJsonable: Jsonable[Song]): Jsonable[PlaylistQueue] =
    new Jsonable[PlaylistQueue] {
      override def jsonify(p: PlaylistQueue): JsValue = p.songs.jsonify
      override def parse(json: JsValue): PlaylistQueue = json.parse[Seq[Song]] |> PlaylistQueue.apply
    }
}
