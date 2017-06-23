package search

import java.io.File

import common.Jsonable
import common.RichJson._
import models.{Album, Artist, Song}
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.{JsArray, JsObject, Json}

object ModelsJsonable {
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
      new Song(file = new File(json str "file"), title = json str "title", artistName = json str "artistName",
        albumName = json str "albumName", track = json int "track", year = json int "year", bitrate = json str "bitrate",
        duration = json int "duration", size = json int "size", discNumber = json ostr "discNumber",
        trackGain = json.\("trackGain").asOpt[Double] map (_.as[Double]))
  }

  implicit object AlbumJsonifier extends Jsonable[Album] {
    def jsonify(a: Album) = Json obj(
        "dir" -> a.dir.getAbsolutePath,
        "title" -> a.title,
        "artistName" -> a.artistName,
        "year" -> a.year)
    def parse(json: JsObject): Album = {
      new Album(new File(json str "dir"),
        title = json str "title",
        artistName = json str "artistName",
        year = json int "year")
    }
  }

  implicit object ArtistJsonifier extends Jsonable[Artist] {
    def jsonify(a: Artist) = Json obj(
        "name" -> a.name,
        "albums" -> AlbumJsonifier.jsonify(a.albums))
    def parse(json: JsObject): Artist = {
      val albums: Seq[Album] = json objects "albums" map AlbumJsonifier.parse
      Artist(json str "name", albums.toSet)
    }
  }
}
