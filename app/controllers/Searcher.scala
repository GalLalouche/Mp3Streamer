package controllers

import java.io.File
import java.net.URLDecoder
import common.rich.path.RichFile.richFile
import models.Song
import play.api.libs.json.{ JsArray, JsObject, Json }
import play.api.mvc.{ Action, Controller }
import search.TermIndexBuilder
import search.MetadataCacher
import common.SimpleActor
import search.Index

object Searcher extends Controller {
  private val indexBuilder = TermIndexBuilder
  var index = indexBuilder.buildIndexFor(MetadataCacher.load)
  def update(songs: TraversableOnce[Song]) {
    index = indexBuilder.buildIndexFor(songs)
  }

  def search(path: String) = Action {
    val query = URLDecoder.decode(path, "UTF-8")
    val terms = query split " "
    Ok(JsArray(index.findIntersection(terms).map(_.jsonify)))
  }
}