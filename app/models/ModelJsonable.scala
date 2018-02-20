package models

import java.io.File

import common.RichJson._
import common.io.{IODirectory, IOFile}
import common.json.{Jsonable, ToJsonableOps}
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.{JsValue, Json}

object ModelJsonable extends ToJsonableOps {
  implicit object SongJsonifier extends Jsonable[Song] {
    override def jsonify(s: Song) = Json obj(
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
    override def parse(json: JsValue): Song = {
      val file = new File(json str "file")
      IOSong(file = IOFile(file), title = json str "title",
        artistName = json str "artistName", albumName = json str "albumName",
        track = json int "track", year = json int "year", bitRate = json str "bitrate",
        duration = json int "duration", size = json int "size", discNumber = json ostr "discNumber",
        trackGain = json.\("trackGain").asOpt[Double])
    }
  }

  implicit object AlbumJsonifier extends Jsonable[Album] {
    override def jsonify(a: Album) = Json obj(
        "dir" -> a.dir.asInstanceOf[IODirectory].path,
        "title" -> a.title,
        "artistName" -> a.artistName,
        "year" -> a.year,
        "songs" -> a.songs)
    override def parse(json: JsValue): Album = {
      new Album(new IODirectory(json str "dir"),
        title = json str "title",
        artistName = json str "artistName",
        year = json int "year",
        songs = json.array("songs").parse[Song])
    }
  }

  implicit object ArtistJsonifier extends Jsonable[Artist] {
    override def jsonify(a: Artist) = Json obj(
        "name" -> a.name,
        "albums" -> a.albums.jsonify)
    override def parse(json: JsValue): Artist = {
      val albums: Seq[Album] = json.value("albums").parse[Seq[Album]]
      Artist(json str "name", albums.toSet)
    }
  }
}
