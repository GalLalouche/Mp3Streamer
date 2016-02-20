package search

import models.MusicFinder
import common.actor.SimpleActor
import models.Song
import java.io.File
import common.rich.path.RichFile._
import play.api.libs.json.JsObject
import play.api.libs.json.Json

object MetadataCacher {
  private val jsonFile = new File("D:/Media/Music/songs.json")

  def save(songs: TraversableOnce[Song]) {
    jsonFile.write(songs.map(_.jsonify).mkString("\n"))
  }
  def load: Seq[Song] = jsonFile.lines
    .map(Json.parse)
    .map(_.as[JsObject])
    .map(Song.apply)
}