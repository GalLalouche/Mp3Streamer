package controllers

import java.net.URLDecoder

import common.concurrency.Extra
import common.rich.RichT.richT
import models.RealLocations
import play.api.Logger
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.{Action, Controller}
import search.Jsonable._
import search.{CompositeIndex, Jsonable}

object Searcher extends Controller with Extra {
  private var index: CompositeIndex = new CompositeIndex(RealLocations)
  override def apply() {
    index = new CompositeIndex(RealLocations)
    Logger info "Search engine has been updated"
  }
  def search(path: String) = Action {
    def toArray[T: Jsonable](results: Seq[T]): JsArray = results.map(implicitly[Jsonable[T]].jsonify).mapTo(JsArray)
    val query = URLDecoder.decode(path, "UTF-8")
    val terms = query split " "
    val (songs, albums, artists) = index.search(terms)

    Ok(Json obj(("songs", toArray(songs)), ("albums", toArray(albums)), ("artists", toArray(artists))))
  }
}
