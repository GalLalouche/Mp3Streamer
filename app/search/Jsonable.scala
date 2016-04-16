package search

import java.io.File

import common.Jsoner._
import models.{Album, Artist, Song}
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}


trait Jsonable[T] {
  def jsonify(t: T): JsObject
  def parse(json: JsObject): T
}

object Jsonable {
  private implicit class foobarz(js: JsObject) {
    def \(s: String): JsValue = js value s
  }
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
        "size" -> s.size)
    def parse(json: JsObject): Song = {
      new Song(file = new File(json \ "file"), title = json \ "title", artistName = json \ "artistName", albumName = json \ "albumName",
        track = json \ "track", year = json \ "year", bitrate = json \ "bitrate",
        duration = json \ "duration", size = json \ "size")
    }
  }

  implicit object AlbumJsonifier extends Jsonable[Album] {
    def jsonify(a: Album) = Json obj(
        "dir" -> a.dir.getAbsolutePath,
        "title" -> a.title,
        "artistName" -> a.artistName,
        "year" -> a.year)
    def parse(json: JsObject): Album = {
      new Album(new File(json \ "dir"),
        title = json \ "title",
        artistName = json \ "artistName",
        year = json \ "year")
    }
  }

  implicit object ArtistJsonifier extends Jsonable[Artist] {
    def jsonify(a: Artist) = Json obj(
        "name" -> a.name,
        "albums" -> JsArray(a.albums.map(AlbumJsonifier.jsonify)))
    def parse(json: JsObject): Artist = {
      val albums = json \ "albums" map (AlbumJsonifier.parse(_))
      new Artist(json \ "name", albums.toSet)
    }
  }

}
