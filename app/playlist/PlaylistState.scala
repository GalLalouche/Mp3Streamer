package playlist


import java.util.concurrent.TimeUnit

import models.Song

import scala.concurrent.duration.Duration

case class PlaylistState(songs: Seq[Song], currentIndex: Int, currentDuration: Duration) {
  require(currentIndex < songs.length && currentIndex >= 0, s"currentIndex <$currentIndex> out of range (0-${songs.length})")
  require(currentDuration != null)
}
object PlaylistState {

  import common.Jsonable
  import common.RichJson._
  import common.rich.RichT._
  import play.api.libs.json.{JsObject, Json}
  import search.ModelsJsonable.SongJsonifier

  implicit object PlaylistStateJsonable extends Jsonable[PlaylistState] {
    override def jsonify(t: PlaylistState): JsObject = Json.obj(
      "songs" -> SongJsonifier.jsonify(t.songs),
      "duration" -> t.currentDuration.toSeconds,
      "currentIndex" -> t.currentIndex)
    override def parse(json: JsObject): PlaylistState = PlaylistState(
      songs = json array "songs" mapTo SongJsonifier.parse,
      currentIndex = json int "currentIndex",
      currentDuration = Duration.apply(json int "duration", TimeUnit.SECONDS))
  }
}

