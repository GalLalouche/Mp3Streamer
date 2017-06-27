package search

import java.io.File

import common.Jsonable
import common.RichJson._
import common.io.{IODirectory, IOFile}
import models.{Album, Artist, IOSong, Song}
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.{JsObject, Json}

object ModelJsonable {
  implicit object SongJsonifier extends Jsonable[Song] {
    def jsonify(s: Song) = Json obj(
        "file" -> s.file.path,
        "title" -> s.title,
        "artistName" -> s.artistName,
        "albumName" -> s.albumName,
        "track" -> s.track,
        "year" -> s.year,
        "bitrate" -> s.bitRate,
        "duration" -> s.duration,
        "size" -> s.size,
        "discNumber" -> s.discNumber,
        "trackGain" -> s.trackGain)
    def parse(json: JsObject): Song = {
      val file = new File(json str "file")
      IOSong(file = IOFile(file), title = json str "title",
        artistName = json str "artistName", albumName = json str "albumName",
        track = json int "track", year = json int "year", bitRate = json str "bitrate",
        duration = json int "duration", size = json int "size", discNumber = json ostr "discNumber",
        trackGain = json.\("trackGain").asOpt[Double])
    }
  }

  implicit object AlbumJsonifier extends Jsonable[Album] {
    def jsonify(a: Album) = Json obj(
        "dir" -> a.dir.asInstanceOf[IODirectory].path,
        "title" -> a.title,
        "artistName" -> a.artistName,
        "year" -> a.year)
    def parse(json: JsObject): Album = {
      new Album(new IODirectory(json str "dir"),
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
