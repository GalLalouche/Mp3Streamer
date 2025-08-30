package backend.module

import java.util.concurrent.TimeUnit

import com.google.inject.Inject
import models.MemorySong
import models.ModelJsonable.SongParser
import play.api.libs.json.JsObject

import scala.concurrent.duration.Duration

import common.json.RichJson.DynamicJson

private class MemorySongParser @Inject() (pathFactory: MemoryPathRefFactory) extends SongParser {
  override def parse(json: JsObject): MemorySong = MemorySong(
    file = pathFactory.parseFilePath(json.str("file")),
    title = json.str("title"),
    artistName = json.str("artistName"),
    albumName = json.str("albumName"),
    trackNumber = json.int("track"),
    year = json.int("year"),
    bitRate = json.str("bitrate"),
    duration = Duration(json.int("duration"), TimeUnit.SECONDS),
    size = json.int("size"),
    discNumber = json.ostr("discNumber"),
    trackGain = json.\("trackGain").asOpt[Double],
    composer = json.\("composer").asOpt[String],
    conductor = json.\("conductor").asOpt[String],
    orchestra = json.\("orchestra").asOpt[String],
    opus = json.\("opus").asOpt[String],
    performanceYear = json.\("performanceYear").asOpt[Int],
  )
}
