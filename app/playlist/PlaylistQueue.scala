package playlist

import models.Song

case class PlaylistQueue(songs: Seq[Song])

object PlaylistQueue {
  import common.Jsonable
  import common.RichJson._
  import common.rich.RichT._
  import play.api.libs.json.{JsArray, JsObject, Json}

  implicit def PlaylistJsonable(implicit songJsonable: Jsonable[Song]): Jsonable[PlaylistQueue] =
    new Jsonable[PlaylistQueue] {
      override def jsonify(p: PlaylistQueue): JsObject =
        Json obj "songs" -> songJsonable.jsonify(p.songs)
      override def parse(json: JsObject): PlaylistQueue =
        json.array("songs") |> songJsonable.parse |> PlaylistQueue.apply
    }
}
