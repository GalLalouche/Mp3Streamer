package models

import java.io.File

import common.rich.path.Directory
import common.rich.path.RichFile._
import play.api.libs.json.JsArray
/**
  * Handles parsing mp3 data
  */
class Album private (val dir: Directory, val name: String, val artist: Artist) {
  lazy val songs = dir.files.filter(x => List("mp3", "flac").contains(x.extension)).map(Song(_)).sortBy(_.track)
  def jsonify = {
    JsArray(songs.map(_.jsonify))
  }
}

object Album {
  def apply(dir: Directory) = {
    new Album(dir, dir.name.split(" ")(1), Artist(dir.parent))
  }
}