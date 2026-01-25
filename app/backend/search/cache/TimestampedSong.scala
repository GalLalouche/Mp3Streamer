package backend.search.cache

import java.time.LocalDateTime

import models.Song
import org.apache.avro.{Schema, SchemaBuilder}
import org.apache.avro.generic.{GenericData, GenericRecord}
import play.api.libs.json.{Json, JsValue}

import common.io.avro.Avroable
import common.io.avro.RichAvro.richGenericRecord
import common.json.Jsonable
import common.json.RichJson.DynamicJson
import common.json.ToJsonableOps._
import common.rich.RichTime._

private[search] case class TimestampedSong(
    updateTime: LocalDateTime,
    song: Song,
)

private object TimestampedSong {
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
  implicit def avroableEv(implicit ev: Avroable[Song]): Avroable[TimestampedSong] =
    new Avroable[TimestampedSong] {
      // override def jsonify(e: TimestampedSong): JsValue = Json.obj(
      //  "updateTime" -> e.updateTime.toMillis,
      //  "song" -> e.song.jsonify,
      // )
      // override def parse(json: JsValue): TimestampedSong = TimestampedSong(
      //  updateTime = json.long("updateTime").toLocalDateTime,
      //  song = json./("song").parse[Song],
      // )
      override def schema: Schema = timestampedSongSchema(ev.schema)
      override def toRecord(a: TimestampedSong): GenericRecord = {
        val $ = new GenericData.Record(schema)
        $.put("updateTime", a.updateTime.toMillis)
        $.put("song", ev.toRecord(a.song))
        $
      }

      override def fromRecord(r: GenericRecord): TimestampedSong = TimestampedSong(
        updateTime = r.getLong("updateTime").toLocalDateTime,
        song = r.parseInner[Song]("song"),
      )
    }

  // TODO cache this somehow
  private def timestampedSongSchema(songSchema: Schema) = SchemaBuilder
    .record("TimestampedSong")
    .namespace("backend.search.cache")
    .fields()
    .requiredLong("updateTime")
    .name("song")
    .`type`(songSchema)
    .noDefault()
    .endRecord()
}
