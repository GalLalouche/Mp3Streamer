package search

import java.io.File

import common.RichJson._
import models.{Album, Artist, Song}
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.{JsArray, JsObject, Json}


trait Jsonable[T] {
  def jsonify(t: T): JsObject
  def jsonify(ts: Seq[T]): JsArray = JsArray(ts map jsonify)
  def parse(json: JsObject): T
  def parse(json: JsArray): Seq[T] = json.value map (_.as[JsObject]) map parse
}

object Jsonable {
  implicit object SongJsonifier extends Jsonable[Song] {
    def jsonify(s: Song) = Json obj(
        "file" -> s.file.getAbsolutePath,
        "title" -> s.title,
        "artistName" -> s.artistName,
        "albumName" -> s.albumName,
        "track" -> s.track,
        "year" -> s.year,
        "bitrate" -> s.bitrate,
        "duration" -> s.duration,
        "size" -> s.size,
        "discNumber" -> s.discNumber,
        "trackGain" -> s.trackGain)
    def parse(json: JsObject): Song =
      new Song(file = new File(json str "file"), title = json str "title", artistName = json str "artistName", albumName = json str "albumName",
        track = json / "track", year = json / "year", bitrate = json str "bitrate",
        duration = json / "duration", size = json / "size", discNumber = json ostr "discNumber",
        trackGain = json.\("trackGain").asOpt[Double] map (_.as[Double]))
  }

  implicit object AlbumJsonifier extends Jsonable[Album] {
    def jsonify(a: Album) = Json obj(
        "dir" -> a.dir.getAbsolutePath,
        "title" -> a.title,
        "artistName" -> a.artistName,
        "year" -> a.year)
    def parse(json: JsObject): Album = {
      new Album(new File(json / "dir"),
        title = json / "title",
        artistName = json / "artistName",
        year = json / "year")
    }
  }

  implicit object ArtistJsonifier extends Jsonable[Artist] {
    def jsonify(a: Artist) = Json obj(
        "name" -> a.name,
        "albums" -> AlbumJsonifier.jsonify(a.albums))
    def parse(json: JsObject): Artist = {
      val albums = json / "albums" map (AlbumJsonifier.parse(_))
      Artist(json / "name", albums.toSet)
    }
  }
}
