package playlist

import java.util.concurrent.TimeUnit

import common.json.ToJsonableOps
import models.Song
import play.api.libs.json.{Format, JsValue}

import scala.concurrent.duration.Duration

private case class PlaylistState(songs: Seq[Song], currentIndex: Int, currentDuration: Duration) {
  require(currentIndex < songs.length && currentIndex >= 0, s"currentIndex <$currentIndex> out of range (0-${songs.length})")
  require(currentDuration != null)
}

private object PlaylistState extends ToJsonableOps {
  import common.RichJson._
  import common.json.Jsonable
  import play.api.libs.json.{JsObject, Json}

  implicit def PlaylistStateJsonable(implicit songJsonable: Format[Song]): Jsonable[PlaylistState] =
    new Jsonable[PlaylistState] {
      override def jsonify(t: PlaylistState): JsObject = Json.obj(
        "songs" -> t.songs.jsonify,
        "duration" -> t.currentDuration.toSeconds,
        "currentIndex" -> t.currentIndex)
      override def parse(json: JsValue): PlaylistState = PlaylistState(
        songs = json.value("songs").parse[Seq[Song]],
        currentIndex = json int "currentIndex",
        currentDuration = Duration.apply(json int "duration", TimeUnit.SECONDS))
    }
}

