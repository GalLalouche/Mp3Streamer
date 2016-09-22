package playlist

import models.Song

case class PlaylistQueue(songs: Seq[Song])

object PlaylistQueue {
  import common.Jsonable
  import common.RichJson._
  import common.rich.RichT._
  import play.api.libs.json.{JsArray, JsObject, Json}
  import search.ModelsJsonable.SongJsonifier

  implicit object PlaylistJsonable extends Jsonable[PlaylistQueue] {
    override def jsonify(p: PlaylistQueue): JsObject = Json obj "songs" -> SongJsonifier.jsonify(p.songs)
    override def parse(json: JsObject): PlaylistQueue = json / "songs" |> (_.as[JsArray]) |> SongJsonifier.parse |> PlaylistQueue.apply
  }
}
