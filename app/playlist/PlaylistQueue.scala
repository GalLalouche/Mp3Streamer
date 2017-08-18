package playlist

import common.Jsonable
import models.Song

case class PlaylistQueue(songs: Seq[Song])

object PlaylistQueue extends Jsonable.ToJsonableOps {
  import common.Jsonable
  import common.RichJson._
  import common.rich.RichT._
  import play.api.libs.json.{JsObject, Json}

  implicit def PlaylistJsonable(implicit songJsonable: Jsonable[Song]): Jsonable[PlaylistQueue] =
    new Jsonable[PlaylistQueue] {
      override def jsonify(p: PlaylistQueue): JsObject = Json obj "songs" -> p.songs.jsonify
      override def parse(json: JsObject): PlaylistQueue =
        json.objects("songs").map(songJsonable.parse) |> PlaylistQueue.apply
    }
}
