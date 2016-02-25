package models

import java.io.File

import common.rich.path.Directory
import common.rich.path.RichFile._
import play.api.libs.json.JsArray
import common.rich.RichT._

/**
  * Handles parsing mp3 data
  */
case class Album(val dir: Directory, val title: String, val artistName: String) {
  lazy val artist: Artist = Artist(dir.parent)
  lazy val songs = dir.files.filter(x => List("mp3", "flac").contains(x.extension)).map(Song(_)).sortBy(_.track)
  lazy val year = songs.head.year
}

object Album {
  def apply(dir: Directory) = new Album(dir, dir.name.replaceAll("\\d+\\w? ?", ""), dir.parent.name)
}