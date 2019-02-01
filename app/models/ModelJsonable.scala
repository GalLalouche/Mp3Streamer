package models

import java.io.File

import common.RichJson._
import common.io.{IODirectory, IOFile}
import common.json.{OJsonable, ToJsonableOps}
import play.api.libs.json.{JsObject, Json}

object ModelJsonable extends ToJsonableOps {
  implicit object SongJsonifier extends OJsonable[Song] {
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
        "trackGain" -> s.trackGain,
        "composer" -> s.composer,
        "opus" -> s.opus,
        "performanceYear" -> s.performanceYear
    )
    override def parse(json: JsObject): Song = {
      val file = new File(json str "file")
      IOSong(file = IOFile(file), title = json str "title",
        artistName = json str "artistName", albumName = json str "albumName",
        track = json int "track", year = json int "year", bitRate = json str "bitrate",
        duration = json int "duration", size = json int "size", discNumber = json ostr "discNumber",
        trackGain = json.\("trackGain").asOpt[Double], composer = json.\("composer").asOpt[String],
        opus = json.\("opus").asOpt[Int], performanceYear = json.\("performanceYear").asOpt[Int],
      )
    }
  }

  implicit object AlbumJsonifier extends OJsonable[Album] {
    override def jsonify(a: Album) = Json obj(
        "dir" -> a.dir.asInstanceOf[IODirectory].path,
        "title" -> a.title,
        "artistName" -> a.artistName,
        "year" -> a.year,
        "songs" -> a.songs.jsonify,
    )
    override def parse(json: JsObject): Album = {
      Album(
        new IODirectory(json str "dir"),
        title = json str "title",
        artistName = json str "artistName",
        year = json int "year",
        songs = json.array("songs").parse[Song],
      )
    }
  }

  implicit object ArtistJsonifier extends OJsonable[Artist] {
    override def jsonify(a: Artist) = Json obj(
        "name" -> a.name,
        "albums" -> a.albums.jsonify)
    override def parse(json: JsObject): Artist = {
      val albums: Seq[Album] = json.value("albums").parse[Seq[Album]]
      Artist(json str "name", albums.toSet)
    }
  }
}
