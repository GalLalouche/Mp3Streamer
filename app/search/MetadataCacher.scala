package search

import models.MusicFinder
import common.SimpleActor
import models.Song
import java.io.File
import common.rich.path.RichFile._
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import models.MusicFinder
import models.MusicFinder
import controllers.Searcher

object MetadataCacher extends SimpleActor[MusicFinder] {
  private val jsonFile = new File("D:/Media/Music/songs.json")
  override protected def act(mf: MusicFinder) {
    save(mf.getSongIterator.toList)
  }
  def save(songs: Seq[Song]) {
    jsonFile.write(songs.map(_.jsonify).mkString("\n"))
    Searcher update songs
  }
  def load: Seq[Song] = jsonFile.lines
    .map(Json.parse)
    .map(_.as[JsObject])
    .map(Song.apply)
}