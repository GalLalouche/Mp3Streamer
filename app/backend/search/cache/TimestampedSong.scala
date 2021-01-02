package backend.search.cache

import java.time.LocalDateTime

import models.Song
import play.api.libs.json.{Json, JsValue}

import common.io.FileRef
import common.json.Jsonable
import common.json.RichJson.DynamicJson
import common.json.ToJsonableOps._
import common.rich.RichTime._

private[search] case class TimestampedSong(
    updateTime: LocalDateTime,
    song: Song,
) {
  def file: FileRef = song.file
}

private[search] object TimestampedSong {
  implicit def jsonableEv(implicit ev: Jsonable[Song]): Jsonable[TimestampedSong] =
    new Jsonable[TimestampedSong] {
      override def jsonify(e: TimestampedSong): JsValue = Json.obj(
        "updateTime" -> e.updateTime.toMillis,
        "song" -> e.song.jsonify,
      )
      override def parse(json: JsValue): TimestampedSong = TimestampedSong(
        updateTime = json.long("updateTime").toLocalDateTime,
        song = json./("song").parse[Song],
      )
    }
}
