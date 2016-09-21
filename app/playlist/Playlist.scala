package playlist

import models.Song

case class Playlist(songs: Seq[Song])

object Playlist {
  import common.Jsonable
  import common.RichJson._
  import common.rich.RichT._
  import play.api.libs.json.{JsArray, JsObject, Json}
  import search.ModelsJsonable.SongJsonifier

  implicit object PlaylistJsonable extends Jsonable[Playlist] {
    override def jsonify(p: Playlist): JsObject = Json obj "songs" -> SongJsonifier.jsonify(p.songs)
    override def parse(json: JsObject): Playlist = json / "songs" |> (_.as[JsArray]) |> SongJsonifier.parse |> Playlist.apply
  }
}
