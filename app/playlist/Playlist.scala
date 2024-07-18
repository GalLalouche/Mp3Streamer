package playlist

import java.util.concurrent.TimeUnit

import models.Song
import play.api.libs.json.JsValue

import scala.concurrent.duration.Duration

import common.json.ToJsonableOps._

private case class Playlist(songs: Seq[Song], currentIndex: Int, currentDuration: Duration) {
  require(
    currentIndex < songs.length && currentIndex >= 0,
    s"currentIndex <$currentIndex> out of range (0-${songs.length})",
  )
  require(currentDuration != null)
}

private object Playlist {
  import play.api.libs.json.{JsObject, Json}

  import common.json.Jsonable
  import common.json.RichJson._

  implicit def PlaylistStateJsonable(implicit songJsonable: Jsonable[Song]): Jsonable[Playlist] =
    new Jsonable[Playlist] {
      override def jsonify(ps: Playlist): JsObject = Json.obj(
        "songs" -> ps.songs.jsonify,
        "duration" -> ps.currentDuration.toSeconds,
        "currentIndex" -> ps.currentIndex,
      )
      override def parse(json: JsValue): Playlist = Playlist(
        songs = json.value("songs").parse[Seq[Song]],
        currentIndex = json.int("currentIndex"),
        currentDuration = Duration.apply(json.int("duration"), TimeUnit.SECONDS),
      )
    }
}
