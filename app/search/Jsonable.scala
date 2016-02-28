package search

import java.io.File

import common.rich.path.Directory
import common.rich.path.RichFile.richFile
import models.{ Album, Artist, Song }
import play.api.libs.json.{ JsArray, JsObject, Json }
import play.api.libs.json.Json.toJsFieldJsValueWrapper

trait Jsonable[T] {
  def jsonify(t: T): JsObject
  def parse(json: JsObject): T
}

object Jsonable {
  private implicit class smartJson(json: JsObject) {
    def string(s: String): String = json.\(s).as[String]
    def int(s: String) = json.\(s).as[Int]
    def long(s: String) = json.\(s).as[Long]
    def array(s: String) = json.\(s).as[JsArray].value
  }
  implicit object SongJsonifier extends Jsonable[Song] {
    def jsonify(s: Song) = Json obj (
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
      val file = new File(json.string("file"))
      new Song(file = file, title = json.string("title"), artistName = json.string("artistName"), albumName = json.string("albumName"),
        track = json.int("track"), year = json.int("year"), bitrate = json.string("bitrate"),
        duration = json.int("duration"), size = json.long("size"))
    }
  }
  implicit object AlbumJsonifier extends Jsonable[Album] {
    def jsonify(a: Album) = Json obj (
      "dir" -> a.dir.getAbsolutePath,
      "title" -> a.title,
      "artistName" -> a.artistName,
      "year" -> a.year)
    def parse(json: JsObject): Album = {
      new Album(new File(json.string("dir")),
          title = json.string("title"),
          artistName = json.string("artistName"),
          year = json.int("year"))
    }
  }
  implicit object ArtistJsonifier extends Jsonable[Artist] {
    def jsonify(a: Artist) = Json obj (
      "name" -> a.name,
      "albums" -> JsArray(a.albums.toSeq.map(AlbumJsonifier.jsonify))
    )
    def parse(json: JsObject): Artist = {
      val albums = json.array("albums").map(_.as[JsObject]).map(AlbumJsonifier.parse)
      new Artist(json.string("name"), albums.toSet)
    }
  }
}
