package models

import java.util.concurrent.TimeUnit

import com.google.inject.Inject
import models.ModelJsonable.SongParser
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.duration.Duration

import common.io.{IOFile, PathRefFactory}
import common.json.OJsonable
import common.json.RichJson._
import common.json.ToJsonableOps._

class ModelJsonable @Inject() (
    songParser: SongParser,
    pathRefParser: PathRefFactory,
) {
  implicit val songJsonifier: OJsonable[Song] = new OJsonable[Song] {
    override def jsonify(s: Song): JsObject = Json.obj(
      "file" -> s.file.path,
      "title" -> s.title,
      "artistName" -> s.artistName,
      "albumName" -> s.albumName,
      "track" -> s.trackNumber,
      "year" -> s.year,
      "bitrate" -> s.bitRate,
      "duration" -> s.duration.toSeconds,
      "size" -> s.size,
      "discNumber" -> s.discNumber,
      "trackGain" -> s.trackGain,
      "composer" -> s.composer,
      "conductor" -> s.conductor,
      "orchestra" -> s.orchestra,
      "opus" -> s.opus,
      "performanceYear" -> s.performanceYear,
    )
    override def parse(json: JsObject): Song = songParser.parse(json)
  }

  implicit val albumDirJsonifier: OJsonable[AlbumDir] = new OJsonable[AlbumDir] {
    override def jsonify(a: AlbumDir): JsObject = Json.obj(
      "dir" -> a.dir.path,
      "title" -> a.title,
      "artistName" -> a.artistName,
      "year" -> a.year,
      "songs" -> a.songs.jsonify,
    )
    override def parse(json: JsObject): AlbumDir = AlbumDir(
      pathRefParser.parseDirPath(json.str("dir")),
      title = json.str("title"),
      artistName = json.str("artistName"),
      year = json.int("year"),
      songs = json.array("songs").parse[Song],
    )
  }

  implicit val artistDirJsonifier: OJsonable[ArtistDir] =
    new OJsonable[ArtistDir] {
      override def jsonify(a: ArtistDir): JsObject =
        Json.obj("dir" -> a.dir.path, "name" -> a.name, "albums" -> a.albums.jsonify)
      override def parse(json: JsObject): ArtistDir = {
        val albums: Seq[AlbumDir] = json.value("albums").parse[Seq[AlbumDir]]
        ArtistDir(pathRefParser.parseDirPath(json.str("dir")), json.str("name"), albums.toSet)
      }
    }
}
object ModelJsonable {
  trait SongParser {
    def parse(json: JsObject): Song
  }
  object IOSongJsonParser extends SongParser {
    override def parse(json: JsObject): IOSong = IOSong(
      file = IOFile(json.str("file")),
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
}
