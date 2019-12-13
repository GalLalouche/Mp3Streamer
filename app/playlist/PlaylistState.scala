package playlist

import java.util.concurrent.TimeUnit

import models.Song
import play.api.libs.json.JsValue

import scala.concurrent.duration.Duration

import common.json.ToJsonableOps._

private case class PlaylistState(songs: Seq[Song], currentIndex: Int, currentDuration: Duration) {
  require(currentIndex < songs.length && currentIndex >= 0, s"currentIndex <$currentIndex> out of range (0-${songs.length})")
  require(currentDuration != null)
}

private object PlaylistState {
  import play.api.libs.json.{JsObject, Json}

  import common.json.RichJson._
  import common.json.Jsonable

  implicit def PlaylistStateJsonable(implicit songJsonable: Jsonable[Song]): Jsonable[PlaylistState] =
    new Jsonable[PlaylistState] {
      override def jsonify(ps: PlaylistState): JsObject = Json.obj(
        "songs" -> ps.songs.jsonify,
        "duration" -> ps.currentDuration.toSeconds,
        "currentIndex" -> ps.currentIndex,
      )
      override def parse(json: JsValue): PlaylistState = PlaylistState(
        songs = json.value("songs").parse[Seq[Song]],
        currentIndex = json int "currentIndex",
        currentDuration = Duration.apply(json int "duration", TimeUnit.SECONDS),
      )
    }
}

