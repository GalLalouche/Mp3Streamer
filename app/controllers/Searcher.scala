package controllers

import java.net.URLDecoder

import common.Jsonable
import common.concurrency.Extra
import play.api.Logger
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.{Action, Controller}
import search.CompositeIndex
import search.ModelsJsonable._

object Searcher extends Controller with Extra
    with Jsonable.ToJsonableOps {
  private implicit val c = Utils.config
  import c._
  private var index: CompositeIndex = new CompositeIndex
  override def apply() {
    index = new CompositeIndex
    Logger info "Search engine has been updated"
  }
  def search(path: String) = Action {
    def toJsArray[T: Jsonable](results: Seq[T]): JsArray = results.jsonify
    val terms = URLDecoder.decode(path, "UTF-8") split " " map (_.toLowerCase)
    val (songs, albums, artists) = index search terms

    Ok(Json obj(("songs", toJsArray(songs)), ("albums", toJsArray(albums)), ("artists", toJsArray(artists))))
  }
}
