package backend.module

import java.util.concurrent.TimeUnit

import com.google.inject.Inject
import models.MemorySong
import org.apache.avro.generic.GenericRecord

import scala.concurrent.duration.Duration

import common.io.avro.ModelAvroable.SongParser
import common.io.avro.RichAvro.richGenericRecord
import common.test.memory_ref.MemoryRefFactory

private class MemorySongAvroableParser @Inject() (pathFactory: MemoryRefFactory)
    extends SongParser {
  override def parse(r: GenericRecord): MemorySong = MemorySong(
    file = pathFactory.parseFilePath(r.getString("file")),
    title = r.getString("title"),
    artistName = r.getString("artistName"),
    albumName = r.getString("albumName"),
    trackNumber = r.getInt("track"),
    year = r.getInt("year"),
    bitRate = r.getString("bitrate"),
    duration = Duration(r.getInt("duration"), TimeUnit.SECONDS),
    size = r.getLong("size"),
    discNumber = r.optString("discNumber"),
    trackGain = r.optDouble("trackGain"),
    composer = r.optString("composer"),
    conductor = r.optString("conductor"),
    orchestra = r.optString("orchestra"),
    opus = r.optString("opus"),
    performanceYear = r.optInt("performanceYear"),
  )
}
