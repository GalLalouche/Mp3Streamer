package search

import play.api.libs.json.JsObject
import models._
import common.rich.path.RichFile._
import java.io.File
import play.api.libs.json.Json
import common.rich.path.Directory

trait Jsonable[T] {
  def jsonify(t: T): JsObject
  def parse(json: JsObject): T
}

object Jsonable {
  implicit object SongJsonifier extends Jsonable[Song] {
    def jsonify(s: Song) = Json obj (
      "file" -> s.file.path,
      "title" -> s.title,
      "artist" -> s.artist,
      "album" -> s.albumName,
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
      new Song(file = file, title = asString("title"), artist = asString("artist"), albumName = asString("albumName"),
        track = asInt("track"), year = asInt("year"), bitrate = asString("bitrate"),
        duration = asInt("duration"), size = asInt("size"))
    }
  }
  implicit object AlbumJsonifier extends Jsonable[Album] {
    def jsonify(a: Album) = Json obj (
      "dir" -> a.dir.path,
      "name" -> a.name,
      "artistName" -> a.artistName)
    def parse(json: JsObject): Album = {
      def asString(s: String): String = json.\(s).as[String]
      def asInt(s: String): Int = json.\(s).as[Int]
      def asLong(s: String): Long = json.\(s).as[Long]

      new Album(Directory(asString("dir")), asString("name"), asString("artistName"))
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
