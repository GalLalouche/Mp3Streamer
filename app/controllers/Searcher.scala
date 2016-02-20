package controllers

import java.io.File
import java.net.URLDecoder

import common.rich.path.RichFile.richFile
import models.Song
import play.api.libs.json.{ JsArray, JsObject, Json }
import play.api.mvc.{ Action, Controller }
import search.TermIndexBuilder

object Searcher extends Controller {
  lazy val songs = new File("D:/Media/Music/songs.json")
      .lines
      .map(Json.parse)
      .map(_.as[JsObject])
      .map(Song.apply)
  lazy val indexBuilder = TermIndexBuilder 
  lazy val index = TermIndexBuilder.buildIndexFor(songs)
  
  def search(path: String) = Action {
    val query = URLDecoder.decode(path, "UTF-8")
    val terms = query split " "
    Ok(JsArray(index.findIntersection(terms).map(_.jsonify)))
  }
}