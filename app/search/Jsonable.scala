package search

import java.io.File

import common.rich.path.Directory
import common.rich.path.RichFile.richFile
import models.{ Album, Artist, Song }
import play.api.libs.json.{ JsObject, Json }
import play.api.libs.json.Json.toJsFieldJsValueWrapper

trait Jsonable[T] {
  def jsonify(t: T): JsObject
  def parse(json: JsObject): T
}

object Jsonable {
  implicit object SongJsonifier extends Jsonable[Song] {
    def jsonify(s: Song) = Json obj (
      "file" -> s.file.path,
      "title" -> s.title,
      "artistName" -> s.artistName,
      "albumName" -> s.albumName,
      "track" -> s.track,
      "year" -> s.year,
      "bitrate" -> s.bitrate,
      "duration" -> s.duration,
      "size" -> s.size)
    def parse(json: JsObject): Song = {
      def asString(s: String): String = json.\(s).as[String]
      def asInt(s: String): Int = json.\(s).as[Int]
      def asLong(s: String): Long = json.\(s).as[Long]

      val file = new File(asString("file"))
      new Song(file = file, title = asString("title"), artistName = asString("artistName"), albumName = asString("albumName"),
        track = asInt("track"), year = asInt("year"), bitrate = asString("bitrate"),
        duration = asInt("duration"), size = asInt("size"))
    }
  }
  implicit object AlbumJsonifier extends Jsonable[Album] {
    def jsonify(a: Album) = Json obj (
      "dir" -> a.dir.path,
      "title" -> a.title,
      "artistName" -> a.artistName,
      "year" -> a.year)
    def parse(json: JsObject): Album = {
      def asString(s: String): String = json.\(s).as[String]
      def asInt(s: String): Int = json.\(s).as[Int]
      def asLong(s: String): Long = json.\(s).as[Long]

      new Album(Directory(asString("dir")), title = asString("title"), artistName = asString("artistName"))
    }
  }
  implicit object ArtistJsonifier extends Jsonable[Artist] {
    def jsonify(a: Artist) = Json obj (
      "dir" -> a.dir.path,
      "name" -> a.name)
    def parse(json: JsObject): Artist = {
      def asString(s: String): String = json.\(s).as[String]
      def asInt(s: String): Int = json.\(s).as[Int]
      def asLong(s: String): Long = json.\(s).as[Long]

      new Artist(Directory(asString("dir")), asString("name"))
    }
  }
}
